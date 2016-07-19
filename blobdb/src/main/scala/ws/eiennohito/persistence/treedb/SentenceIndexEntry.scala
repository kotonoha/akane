package ws.eiennohito.persistence.treedb

import java.nio.ByteBuffer

import ws.kotonoha.akane.blobdb.impl.bgz.BlockCompressedFilePointerUtil

/**
  * @author eiennohito
  * @since 2016/07/19
  */
case class SentenceIndexEntry(file: Int, ptr: Long, len: Int) {
  def fullyInBlock: Boolean = {
    val blockSize = 64 * 1024
    val posInBlock = BlockCompressedFilePointerUtil.getBlockOffset(ptr)
    val remBytes = blockSize - posInBlock
    remBytes >= len
  }
}

trait ResultCreator[T] {
  def result(buf: ByteBuffer): Option[T]
}

class TreeDatabaseException(msg: String) extends RuntimeException(msg)
