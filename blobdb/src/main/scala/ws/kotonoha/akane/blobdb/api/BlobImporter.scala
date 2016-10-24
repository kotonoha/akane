package ws.kotonoha.akane.blobdb.api

import ws.kotonoha.akane.blobdb.impl.BlobIndexEntry

/**
  * @author eiennohito
  * @since 2016/07/19
  */
case class DataRef[Key <: AnyRef](id: Key, ptr: BlobIndexEntry, writtenBytes: Long)

final class IdRef[Key <: AnyRef](f: IdOps[Key], val prefix: Int) {
  private[this] var inst: Key = null.asInstanceOf[Key]

  def get(): Key = {
    val o = inst
    if (o == null) {
      val g = f.generate(prefix)
      inst = g
      g
    } else {
      if (f.checkPrefix(o, prefix)) {
        o
      } else {
        throw new Exception(s"invalid prefix $prefix for key $o")
      }
    }
  }

  def set(k: Key): Unit = inst = k

  def reset(): Unit = {
    inst = null.asInstanceOf[Key]
  }
}
