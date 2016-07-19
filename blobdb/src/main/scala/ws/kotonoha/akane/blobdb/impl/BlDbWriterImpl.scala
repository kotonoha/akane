package ws.kotonoha.akane.blobdb.impl

import java.io.{Closeable, OutputStream}
import java.nio.ByteBuffer
import java.util.concurrent.ConcurrentLinkedQueue

import com.typesafe.scalalogging.StrictLogging
import ws.eiennohito.persistence.treedb.SentenceIndexEntry
import ws.kotonoha.akane.blobdb.api._

import scala.concurrent.{Future, Promise}



private[impl] trait CompressedShardWriter {
  def file: Int

  def blockAddress: Long
  def position: Long
  def stream: OutputStream

  def commit(refs: Seq[DataRef[_]]): Future[TrOk]
  def goToDaddy(): Unit
}

class BlobImporterImpl[K <: AnyRef](
  idref: IdRef[K],
  transformer: BlobTransformer[K],
  writer: CompressedShardWriter
) extends BlobImporter[K] {
  private var committed = false
  private var lastAddr = writer.blockAddress
  private var saveCnt = 0

  override def pushData(data: ByteBuffer): Option[DataRef[K]] = {
    assert(!committed, "blob importer should not be committed")
    val pos = writer.position
    idref.reset()
    val len = transformer.transformWrite(idref, data, writer.stream)
    if (len <= 0) {
      None
    } else {
      saveCnt += 1
      val idxEntry = SentenceIndexEntry(writer.file, pos, len)
      val lastOne = lastAddr
      lastAddr = writer.blockAddress
      Some(DataRef(idref.get(), idxEntry, lastAddr - lastOne))
    }
  }

  override def commit(refs: Seq[DataRef[K]]): Future[TrOk] = {
    val reply = writer.commit(refs)
    writer.goToDaddy()
    committed = true
    reply
  }

  override def rollback() = {
    writer.goToDaddy()
    committed = true
    Future.successful(TrOk)
  }
}

class BlDbWriterImpl[K <: AnyRef](impl: BlDbImpl[K], cfg: BlobDbConfig, ops: IdOps[K], dtf: BlobTransformer[K])
  extends BlobDbWriter[K] with Closeable with StrictLogging {
  private[this] val actor = cfg.forCommit.actorOf(DbWriterActor.props(impl))


  @volatile
  private var streamCache = new ConcurrentLinkedQueue[CompressedShardWriter]()

  private def createWriter() = {
    val fileNo = impl.nextFileNo()

    logger.trace(s"creating writer #$fileNo")
    val name = cfg.pathFile(fileNo)
    val wr0 = cfg.compr.writer(name)
    new CompressedShardWriter { self =>
      override def file = fileNo
      override def position = wr0.position
      override def blockAddress = wr0.blockAddress
      override def stream = wr0.stream
      override def goToDaddy() = returnWriter(self)
      override def commit(refs: Seq[DataRef[_]]) = {
        val p = Promise[TrOk]
        actor ! DbWriterActor.Commit(refs, p)
        p.future
      }
    }
  }

  def returnWriter(cw: CompressedShardWriter): Unit = synchronized {
    if (streamCache == null) {
      throw new IllegalStateException("db creator is closed")
    } else {
      logger.trace(s"returning writer ${cw.file}")
      if (cw.blockAddress > cfg.shardMaxSize) {
        logger.trace(s"writer ${cw.file} is over quota, closing it")
        cw.stream.close()
      } else {
        streamCache.offer(cw)
      }
    }
  }

  def writer() = synchronized {
    if (streamCache == null) {
      throw new IllegalStateException("db creator is closed")
    } else {
      val item = streamCache.poll()
      if (item == null) {
        createWriter()
      } else {
        logger.trace(s"writer ${item.file} from cache")
        item
      }
    }
  }


  override def delete(ids: Seq[K]) = {
    val p = Promise[TrOk]
    actor ! DbWriterActor.Delete(ids, p)
    p.future
  }

  override def makeImporter(tf: BlobTransformer[K]) = {
    val idref = new IdRef[K](ops, cfg.idPrefix)
    new BlobImporterImpl[K](
      idref,
      tf,
      writer()
    )
  }

  def defaultImporter() = makeImporter(dtf)

  def optimize(): Unit = impl.db.compact()

  override def close() = {
    val data = streamCache
    streamCache = null
    cfg.forCommit.stop(actor)
    val it = streamCache.iterator()
    while (it.hasNext) {
      it.next().stream.close()
    }
  }
}
