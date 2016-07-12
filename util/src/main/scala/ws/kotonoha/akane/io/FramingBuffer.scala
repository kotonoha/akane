package ws.kotonoha.akane.io

import java.io.{Closeable, InputStream}
import java.nio.ByteBuffer
import java.util.concurrent.{Future => JFuture}

/**
 * @author eiennohito
 * @since 2015/10/07
 */
trait FramingBuffer extends Closeable {
  def data: ByteBuffer

  /**
   * @return false if EOF
   */
  def startFrame(): Boolean

  /**
   * @return true if the end currently in buffer and
   *         no calls to continueFrame is required to finish
   */
  def isEndVisible: Boolean

  /**
   *
   * @return false if EOF
   */
  def continueFrame(): Boolean

  /**
   * Call this before moving to next frame
   */
  def endFrame(): Unit
}

final class InputStreamFramingBuffer(
  input: InputStream, separator: Array[Byte], size: Int = 1024 * 1024
) extends FramingBuffer {
  assert(separator.length < size + 1, "buffer should be at least as long as separator")
  val buffer = ByteBuffer.allocate(size)

  var lastRead = 0

  doRead()

  override def data = {
    val buf = buffer.duplicate()
    buf.limit(sepIdx)
    buf
  }

  def doRead(): Unit = {
    lastRead = input.read(buffer.array(), buffer.position(), buffer.remaining())
    if (lastRead > 0) {
      buffer.limit(buffer.position() + lastRead)
    } else {
      buffer.limit(buffer.position())
    }
  }

  override def continueFrame(): Boolean = {
    if (lastRead < 0) {
      return false
    }

    buffer.position(sepIdx)
    assert(buffer.remaining() <= separator.length)
    update()
    sepIdx = 0
    if (resolveFrame() && !isEndVisible) {
      sepIdx = buffer.limit() - separator.length
    }

    true
  }

  def update(): Unit = {
    if (lastRead != -1) {
      buffer.compact()
      doRead()
      buffer.position(0)
    }
  }

  var sepIdx = 0
  var haveEnd = false

  override def startFrame() = {
    if (buffer.remaining() < separator.length) {
      update()
    }
    if (resolveFrame()) {
      if (!isEndVisible) {
        sepIdx = buffer.limit() - separator.length
      } //otherwise it was placed by resolveFrame
      true
    } else false
  }


  override def endFrame() = {
    buffer.position(sepIdx)
    val start = sepIdx
    if (start > size / 2) {
      update()
      sepIdx = 0
    }
  }

  /**
   * @return false if EOF
   */
  def resolveFrame(): Boolean = {
    val start = sepIdx
    val pos = BufferUtils.indexOf(buffer, start, buffer.limit(), separator)
    if (pos != -1) {
      haveEnd = true
      sepIdx = pos + separator.length
      true
    } else {
      haveEnd = false
      sepIdx = buffer.limit()
      lastRead >= 0
    }
  }

  override def isEndVisible = haveEnd

  override def close() = input.close()

}


trait FramingBufferX extends Closeable {

  /**
   *
   * @return read only byte buffer of content
   *         Do not cache it
   */
  def data: ByteBuffer

  /**
   * Goes to next frame
   *
   * @return true if there could be more elements
   */
  def start(): Boolean

  /**
   *
   * @return true if all frame is in memory currently.
   *         In this case #resolveRemaining() is a noop.
   */
  def inMemory(): Boolean


  /**
   * Tries to move more data to memory
   *
   * @return true if got to end
   */
  def resolveRemaining(): Boolean
}


