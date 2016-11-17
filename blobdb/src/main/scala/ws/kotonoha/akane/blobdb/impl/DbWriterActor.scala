package ws.kotonoha.akane.blobdb.impl

import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.StrictLogging
import org.mapdb.BTreeMap
import ws.kotonoha.akane.blobdb.api.{DataRef, IdOps, TrOk}

import scala.concurrent.Promise

/**
  * @author eiennohito
  * @since 2016/07/19
  */
private[impl] class DbWriterActor[K <: AnyRef](dbi: DbImplApi[K], ops: IdOps[K]) extends Actor with StrictLogging {
  @inline
  final def transaction[T](p: Promise[TrOk])(f: => T): Unit = {
    try {
      f
      dbi.db.commit()
      p.success(TrOk)
    } catch {
      case e: Exception =>
        logger.error("can't execute transaction", e)
        dbi.db.rollback()
        p.failure(e)
    }
  }

  private val idx = dbi.index.asInstanceOf[BTreeMap[AnyRef, BlobIndexEntry]]

  override def receive = {
    case DbWriterActor.Delete(ids, p) => transaction(p) {
      ids.foreach(id => idx.remove(id))
    }
    case DbWriterActor.Commit(items, p, fileNo) => transaction(p) {
      items.foreach { d =>
        //logger.debug(s"insert id=${ops.debug(d.id.asInstanceOf[K])}")
        idx.put(d.id.asInstanceOf[AnyRef], d.ptr)
      }
      dbi.invalidateShard(fileNo)
    }
  }
}

private[impl] object DbWriterActor {
  def props[K <: AnyRef](impl: DbImplApi[K]): Props = Props(new DbWriterActor[K](impl, impl.ops))

  case class Delete(ids: Seq[AnyRef], p: Promise[TrOk])
  case class Commit(data: Seq[DataRef[_]], p: Promise[TrOk], fileNo: Int)
}

