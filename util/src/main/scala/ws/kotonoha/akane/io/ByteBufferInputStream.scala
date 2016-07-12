package ws.kotonoha.akane.io

import java.io.InputStream
import java.nio.ByteBuffer

/**
  * @author eiennohito
  * @since 2015/12/09
  */
class ByteBufferInputStream(buf: ByteBuffer) extends InputStream {
  override def read(): Int = {
    if (buf.remaining() == 0) return -1
    buf.get()
  }
  override def read(b: Array[Byte]): Int = {
    if (buf.remaining() == 0) return -1
    val pos = buf.position()
    val toRead = b.length min buf.remaining()
    buf.get(b, 0, toRead)
    toRead
  }
  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    if (buf.remaining() == 0) return -1
    val pos = buf.position()
    val toRead = len min buf.remaining()
    buf.get(b, off, toRead)
    toRead
  }

  override def available() = {
    buf.remaining()
  }

  override def skip(n: Long) = {
    assert(n <= available())
    buf.position(buf.position() + n.toInt)
    n
  }
}
