package ws.kotonoha.akane.blobdb.impl

import ws.kotonoha.akane.blobdb.impl.bgz.BlockCompressedFilePointerUtil

/**
  * @author eiennohito
  * @since 2016/07/19
  */
case class BlobIndexEntry(file: Int, ptr: Long, len: Int) {
  def fullyInBlock: Boolean = {
    val blockSize = 64 * 1024
    val posInBlock = BlockCompressedFilePointerUtil.getBlockOffset(ptr)
    val remBytes = blockSize - posInBlock
    remBytes >= len
  }
}

class TreeDatabaseException(msg: String) extends RuntimeException(msg)
