package ws.kotonoha.akane.io

import java.io.InputStream
import java.nio.ByteBuffer
import java.util

import ws.kotonoha.akane.unicode.impl.UnicodeUtilLowLevel

/**
 * @author eiennohito
 * @since 15/08/11
 */
final class GrowableByteBuffer(initSize: Int = 4096) {

  var array = new Array[Byte](initSize)

  def asBuffer = ByteBuffer.wrap(array, 0, position)

  var position = 0
  def ensureSize(size: Int) = {
    if (array.length < size)
      array = grow(size)
  }

  private[this] def grow(size: Int) = {
    val nextSize = size * 5 / 3 + 24
    util.Arrays.copyOf(array, nextSize)
  }

  def reset(): Unit = {
    position = 0
  }

  def capacity = array.length

  def append(bytes: Array[Byte], offset: Int, len: Int): Unit = {
    ensureSize(position + bytes.length)
    System.arraycopy(bytes, offset, array, position, len)
    position += len
  }

  def append(buf: ByteBuffer): Int = {
    val len = buf.remaining()
    ensureSize(position + len)
    buf.get(array, position, len)
    position += len
    len
  }

  //append up to 16M by default
  def append(buf: FramingBuffer, maxBuffer: Long = 16 * 1024 * 1024): Unit = {
    var size: Long = append(buf.data)
    if (!buf.isEndVisible) {
      do {
        if (!buf.continueFrame()) {
          return
        }
        size += append(buf.data)
        if (size > maxBuffer) {
          throw new BufferAppendOverflowException(s"$size was more than $maxBuffer")
        }
      } while (!buf.isEndVisible)
    }
  }

  def readFully(is: InputStream): Unit = {
    while (true) {
      ensureSize(capacity + 4096)
      val available = capacity - position
      val read = is.read(array, position, available)
      if (read == -1) {
        return
      } else {
        position += read
      }
    }
  }

  def u8String = new String(array, 0, position, "utf-8")
}

final class DataWriter(val buf: GrowableByteBuffer) extends AnyVal {
  def ensureEmpty(sz: Int) = {
    buf.ensureSize(buf.position + sz)
  }

  def writeString(s: CharSequence) = {
    ensureEmpty(s.length() * UnicodeUtilLowLevel.MAX_UTF8_BYTES_PER_CHAR)
    buf.position = UnicodeUtilLowLevel.UTF16toUTF8(s, 0, s.length(), buf.array, buf.position)
  }
}

class BufferAppendOverflowException(msg: String) extends RuntimeException(msg)
