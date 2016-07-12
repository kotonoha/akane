package ws.kotonoha.akane.io

import java.io.InputStream
import java.nio.ByteBuffer

/**
 * @author eiennohito
 * @since 2015/10/08
 */
class FrameAdapter(inp: FramingBuffer) extends CloseableIterator[ByteBuffer] {
  private val buffer = new GrowableByteBuffer()

  override def close() = inp.close()

  private var calledNext = false

  override def next() = {
    calledNext = true
    if (inp.isEndVisible) {
      inp.data
    } else {
      buffer.reset()
      buffer.append(inp)
      buffer.asBuffer
    }
  }

  private var nextItem = inp.startFrame()

  override def hasNext = {
    if (calledNext) {
      calledNext = false
      inp.endFrame()
      nextItem = inp.startFrame()
    }

    nextItem
  }
}

object FrameAdapter {
  def apply(is: InputStream, delim: Array[Byte]) = {
    new FrameAdapter(new InputStreamFramingBuffer(is, delim))
  }
}
