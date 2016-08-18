package ws.kotonoha.akane.blobdb.impl

import java.io.{Closeable, RandomAccessFile}
import java.nio.ByteBuffer
import java.util.concurrent.{ConcurrentHashMap, TimeUnit}

import com.github.benmanes.caffeine.cache.Caffeine
import com.typesafe.scalalogging.StrictLogging
import org.mapdb.{BTreeMap, DB, DBMaker}
import ws.kotonoha.akane.blobdb.api._
import ws.kotonoha.akane.blobdb.impl.bgz.BlockCompressedFilePointerUtil
import ws.kotonoha.akane.blobdb.util.StringResultCreator
import ws.kotonoha.akane.io.GrowableByteBuffer
import ws.kotonoha.akane.resources.FSPaths

/**
  * @author eiennohito
  * @since 2016/07/19
  */

private[impl] trait DbImplApi[K <: AnyRef] extends BlobDb[K] {
  //Low-level API for reading blocks
  private[impl] def blockFor(idx: BlobIndexEntry): DecompressedBuffer
  private[impl] def fillInBlock(gbb: GrowableByteBuffer, entry: BlobIndexEntry): Unit

  protected[impl] def db: DB
  private[impl] def index: BTreeMap[K, BlobIndexEntry]
  private[impl] def invalidateShard(file: Int): Unit
  private[impl] def nextFileNo(): Int
}

class BlDbImpl[Key <: AnyRef](cfg: BlobDbConfig, val ops: IdOps[Key], defaultTransform: BlobTransformer[Key])
  extends Closeable with StrictLogging with DbImplApi[Key] {

  private final val blockSize = 64 * 1024

  private val bufferCache = {
    val size = cfg.decompressedCache / blockSize
    logger.trace(s"storing up to $size blocks of 64k")
    val bldr = Caffeine.newBuilder()
    bldr.maximumSize(size)
      .expireAfterAccess(20, TimeUnit.MINUTES)
      .executor(cfg.cacheEc)
      .build[BufferPointer, DecompressedBuffer]
  }

  protected def root = cfg.root
  protected val indexPath = root.resolve("index.db")

  protected[impl] val db = {
    import language.existentials
    val f = DBMaker.newFileDB(indexPath.toFile)
    f.mmapFileEnableIfSupported()
    f.make()
  }


  override def invalidateShard(file: Int) = {
    treeBuffers.get(file) match {
      case null => //nothing to do
      case buf => buf.invalidate()
    }
  }

  override def close() = {
    db.close()

    val wr = cachedWriter
    if (wr != null) {
      cachedWriter = null
      wr.close()
    }

    val iter = treeBuffers.values().iterator()
    while (iter.hasNext) {
      iter.next().close()
    }
  }

  private[this] val treeBuffers = new ConcurrentHashMap[Int, MapBufferWithCache]()

  protected[impl] val index = db.createTreeMap("index")
    .keySerializer(ops.serializer)
    .valueSerializer(new SentenceIndexEntrySerializer)
    .comparator(ops.comparator)
    .makeOrGet[Key, BlobIndexEntry]()

  private[this] val fileCount = db.getAtomicInteger("fileCounter")

  private[impl] def nextFileNo(): Int = fileCount.getAndIncrement()

  private def buffer(fileNo: Int): MapBufferWithCache = {
    val info = treeBuffers.get(fileNo)
    if (info == null) {
      val created = {
        val path = cfg.guessFileForNum(fileNo)
        val raf = new RandomAccessFile(path.toFile, "r")
        val kind = BlobDbCodec.guess(FSPaths.extension(path), cfg)
        new MapBufferWithCache(fileNo, raf, kind.reader(), bufferCache)
      }
      val res = treeBuffers.put(fileNo, created)
      if (res != null) { //can create two buffers concurrently
        res.close()
      }
      created
    } else info
  }

  private[impl] override def fillInBlock(gbb: GrowableByteBuffer, entry: BlobIndexEntry): Unit = {
    var remaining = entry.len
    gbb.ensureSize(remaining)
    val buf = buffer(entry.file)
    var addr = BlockCompressedFilePointerUtil.getBlockAddress(entry.ptr)
    var off = BlockCompressedFilePointerUtil.getBlockOffset(entry.ptr)
    while (remaining > 0) {
      val block = buf.block(addr, buf.reader)
      val data = block.data
      val readable = data.length - off
      val toRead = Math.min(readable, remaining)
      gbb.append(data, off, toRead)
      remaining -= toRead
      addr += block.compressedSize
      off = 0
    }
  }

  private[impl] override def blockFor(entry: BlobIndexEntry): DecompressedBuffer = {
    val buf = buffer(entry.file)
    val addr = BlockCompressedFilePointerUtil.getBlockAddress(entry.ptr)
    buf.block(addr, buf.reader)
  }

  import scala.collection.JavaConverters._


  override def infoFor(id: Key) = Option(index.get(id))

  override def valueGetter[Val](rc: ResultCreator[Val]) = new DefaultSearchImpl[Key, Val](this, rc)
  override def get(id: Key) = valueGetter(new StringResultCreator).get(id)

  override def idCount = index.sizeLong()
  override def idIter() = index.keySet().iterator().asScala

  @volatile private[this] var cachedWriter: BlDbWriterImpl[Key] = null
  override def writer() = synchronized {
    var wr = cachedWriter
    if (wr == null) {
      wr = new BlDbWriterImpl[Key](this, cfg, ops, defaultTransform)
      cachedWriter = wr
    }
    wr
  }
}

private[impl] final class DefaultSearchImpl[Key <: AnyRef, T](dbi: DbImplApi[Key], rc: ResultCreator[T]) extends ItemSearch[Key, T] with StrictLogging {
  private lazy val growBuffer = new GrowableByteBuffer()

  override def get(id: Key) = {
    val nfo = dbi.infoFor(id)
    if (nfo.isDefined) {
      tryRead(id, nfo.get)
    } else None
  }

  private def tryRead(id: Key, ptr: BlobIndexEntry): Option[T] = {
    try {
      val res = doRead(ptr)
      if (res.isEmpty) {
        logger.warn(s"$rc could not read data id=$id")
      }
      res
    } catch {
      case e: Exception =>
        logger.error(s"couldn't deserialize data id=$id", e)
        None
    }
  }

  private def doRead(ptr: BlobIndexEntry): Option[T] = {
    if (ptr.fullyInBlock) {
      useBufferDirectly(ptr)
    } else {
      readSeveralParts(ptr)
    }
  }

  private def useBufferDirectly(ptr: BlobIndexEntry): Option[T] = {
    val block = dbi.blockFor(ptr)
    val pos = BlockCompressedFilePointerUtil.getBlockOffset(ptr.ptr)
    val buffer = ByteBuffer.wrap(block.data, pos, ptr.len)
    rc.result(buffer)
  }

  private def readSeveralParts(ptr: BlobIndexEntry): Option[T] = {
    val buf = growBuffer
    buf.reset()
    dbi.fillInBlock(buf, ptr)
    rc.result(buf.asBuffer)
  }
}






