package ws.kotonoha.akane.io

import java.io.InputStream
import java.nio.ByteBuffer

/**
 * @author eiennohito
 * @since 15/08/11
 */
final class BufferReader(buffer: ByteBuffer, ratio: Double = 0.1) {

  val refresh = (buffer.capacity() * ratio).toInt

  buffer.position(buffer.capacity())

  var eof = false

  private var totalRead = 0L

  @inline
  def fillFrom[T](stream: InputStream)(func: ByteBuffer => T): T = {
    if (buffer.remaining() < refresh && !eof) {
      buffer.compact()
      val read = stream.read(buffer.array(), buffer.position(), buffer.remaining())
      if (read == -1) {
        eof = true
        buffer.flip()
      } else {
        totalRead += read
        val pos = buffer.position()
        buffer.position(0)
        buffer.limit(pos + read)
      }
    }
    func(buffer)
  }

  def readBytes = totalRead
}
