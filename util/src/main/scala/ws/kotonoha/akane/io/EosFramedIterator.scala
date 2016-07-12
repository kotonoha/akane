package ws.kotonoha.akane.io

import java.nio.ByteBuffer

/**
  * @author eiennohito
  * @since 2016/07/12
  */
class EosFramedIterator(adp: FrameAdapter) extends CloseableIterator[ByteBuffer] {
  override def close() = adp.close()

  override def next() = {
    val obj = adp.next()
    myBlocks += 1
    mySize += obj.remaining()
    obj
  }

  override def hasNext = adp.hasNext

  private var myBlocks = 0

  def blocks: Int = myBlocks

  private var mySize = 0L

  def readBytes: Long = mySize
}
