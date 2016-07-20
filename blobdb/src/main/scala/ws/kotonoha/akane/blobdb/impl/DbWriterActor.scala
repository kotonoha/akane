package ws.kotonoha.akane.blobdb.impl

import akka.actor.{Actor, ActorLogging, Props}
import org.mapdb.BTreeMap
import ws.kotonoha.akane.blobdb.api.{DataRef, TrOk}

import scala.concurrent.Promise

/**
  * @author eiennohito
  * @since 2016/07/19
  */
private[impl] class DbWriterActor(dbi: DbImplApi[_]) extends Actor with ActorLogging {
  @inline
  final def transaction[T](p: Promise[TrOk])(f: => T): Unit = {
    try {
      f
      dbi.db.commit()
      p.success(TrOk)
    } catch {
      case e: Exception =>
        log.error(e, "can't execute transaction")
        dbi.db.rollback()
        p.failure(e)
    }
  }

  private val idx = dbi.index.asInstanceOf[BTreeMap[AnyRef, SentenceIndexEntry]]

  override def receive = {
    case DbWriterActor.Delete(ids, p) => transaction(p) {
      ids.foreach(id => idx.remove(id))
    }
    case DbWriterActor.Commit(items, p) => transaction(p) {
      items.foreach(d => idx.put(d.id.asInstanceOf[AnyRef], d.ptr))
    }
  }
}

private[impl] object DbWriterActor {
  def props(impl: DbImplApi[_]): Props = Props(new DbWriterActor(impl))

  case class Delete(ids: Seq[AnyRef], p: Promise[TrOk])
  case class Commit(data: Seq[DataRef[_]], p: Promise[TrOk])
}

