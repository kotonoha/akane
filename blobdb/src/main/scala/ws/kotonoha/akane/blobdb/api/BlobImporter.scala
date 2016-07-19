package ws.kotonoha.akane.blobdb.api

import ws.eiennohito.persistence.treedb.SentenceIndexEntry

/**
  * @author eiennohito
  * @since 2016/07/19
  */



case class DataRef[Key <: AnyRef](id: Key, ptr: SentenceIndexEntry, writtenBytes: Long)

final class IdRef[Key <: AnyRef](f: IdOps[Key], val prefix: Int) {
  private[this] var inst: Key = _

  def get(): Key = {
    val o = inst
    if (o == null) {
      f.generate(prefix)
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
