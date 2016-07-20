package ws.kotonoha.akane.blobdb.impl

import java.io.{Closeable, DataInput, DataOutput, RandomAccessFile}
import java.nio.channels.FileChannel
import java.nio.channels.FileChannel.MapMode
import java.nio.{ByteBuffer, ByteOrder}
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Function

import com.github.benmanes.caffeine.cache.Cache
import org.mapdb.Serializer

/**
 * @author eiennohito
 * @since 2014-08-29
 */

case class BufferDescription(buf: ByteBuffer, offset: Long, len: Int)

final class LargeFileMemoryMapper(raf: RandomAccessFile, file: FileChannel, mode: FileChannel.MapMode, pageOffset: Int, overlap: Int) extends Closeable {

  override def close() = {
    file.close()
  }

  val pageSize = 1 << pageOffset
  val mapSize = pageSize + overlap


  type Buffers = Array[BufferDescription]

  val buffers = new AtomicReference[Buffers](createBuffers)

  private def createBuffers: Buffers = {
    val sz = file.size()
    val cnt = sz / pageSize + 1
    (0L until cnt).map { i =>
      val rest = sz - (pageSize * i)
      val toMap = if (rest < pageSize) rest else mapSize
      val offset = i * pageSize
      val buf = file.map(mode, offset, toMap)
      buf.order(ByteOrder.LITTLE_ENDIAN)
      BufferDescription(buf, offset, toMap.toInt)
    }.toArray
  }

  private def recreateBuffers() = synchronized {
    if (buffers.get() == null) {
      buffers.set(createBuffers)
    }
    buffers.get()
  }

  private def bufferFor(off: Long): ByteBuffer = {
    val idx = posFor(off)
    val buf = buffers.get()
    if (buf != null) {
      buf(idx).buf
    } else {
      recreateBuffers().apply(idx).buf
    }
  }

  private def posFor(off: Long): Int = {
    (off >>> pageOffset).toInt
  }

  private val mask: Long = pageSize - 1

  private var mySize = raf.length()

  def invalidate() = {
    val len = raf.length()
    if (len != mySize) synchronized {
      buffers.set(null)
      mySize = len
    }
  }

  @inline
  def withBuffer[T](pos: Long)(op: (ByteBuffer, Int) => T): T = {
    val buf = bufferFor(pos)
    val off = (pos & mask).toInt
    op(buf, off)
  }
}

class CompressedFileBlockReader(mapper: LargeFileMemoryMapper) extends Closeable {
  override def close() = mapper.close()

  def read(ptr: Long, reader: BlockReader) = {
    mapper.withBuffer(ptr)(reader.readBlock)
  }

  def invalidate() = mapper.invalidate()
}


class SentenceIndexEntrySerializer extends Serializer[SentenceIndexEntry] with Serializable {
  override def serialize(out: DataOutput, value: SentenceIndexEntry) = {
    out.writeInt(value.file)
    out.writeLong(value.ptr)
    out.writeInt(value.len)
  }

  override def fixedSize() = 4 + 8 + 4

  override def deserialize(in: DataInput, available: Int) = {
    val file = in.readInt()
    val ptr = in.readLong()
    val sz = in.readInt()
    SentenceIndexEntry(file, ptr, sz)
  }
}

case class DecompressedBuffer(compressedSize: Int, data: Array[Byte])

class MapBufferWithCache(fileNo: Int, raf: RandomAccessFile, val reader: BlockReader, cache: Cache[BufferPointer, DecompressedBuffer]) extends Closeable {
  val chan = raf.getChannel
  val rdr = new CompressedFileBlockReader(new LargeFileMemoryMapper(raf, chan, MapMode.READ_ONLY, 29, 128 * 1024))


  def block(ptr: Long, reader: BlockReader): DecompressedBuffer = {
    val bptr = BufferPointer(fileNo, ptr)
    cache.get(bptr, new Function[BufferPointer, DecompressedBuffer] {
      override def apply(t: BufferPointer) = {
        rdr.read(ptr, reader)
      }
    })
  }

  def invalidate() = {
    rdr.invalidate()
  }

  override def close() = {
    rdr.close()
    raf.close()
  }
}

case class BufferPointer(file: Int, address: Long)
