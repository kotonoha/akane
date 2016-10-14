package ws.kotonoha.akane.akka

import akka.actor.{Actor, ActorRef, ActorRefFactory, Props}
import akka.routing.RoundRobinPool
import akka.util.Timeout
import ws.kotonoha.akane.analyzers.{AsyncAnalyzer, SubprocessControls, SyncAnalyzer}

import scala.concurrent.{ExecutionContext, Future}
import scala.reflect.ClassTag
import scala.util.{Failure, Success}

/**
  * @author eiennohito
  * @since 2016/10/14
  */
class AnalyzerActor[I: ClassTag, O](factory: => SyncAnalyzer[I, O] with SubprocessControls) extends Actor {
  private var sync = factory
  private val itag = implicitly[ClassTag[I]]

  private def doAnalyze(sndr: ActorRef, ref: AnyRef, x: I) = {
    val res = sync.analyzeSync(x)
    res match {
      case Success(o) => sndr ! AnalyzerActor.Reply(ref, o)
      case Failure(t) => sndr ! AnalyzerActor.Failure(ref, t)
    }
  }

  override def receive: Receive = {
    case AnalyzerActor.Request(ref, data) =>
      data match {
        case itag(x) => doAnalyze(sender(), ref, x)
        case _ => sender() ! AnalyzerActor.Failure(ref, new ClassCastException(s"$data was not $itag, but ${data.getClass}"))
      }
    case AnalyzerActor.RestartSubprocess =>
      sync.close()
      sync = factory
  }

  override def postStop(): Unit = {
    super.postStop()
    sync.close()
  }
}

object AnalyzerActor {
  case class Request[I](ref: AnyRef, data: I)
  case class Reply[O](ref: AnyRef, data: O)
  case class Failure(ref: AnyRef, ex: Throwable)

  case object RestartSubprocess
}

class ActorBackedAsyncAnalyzer[I: ClassTag, O](arf: ActorRefFactory, factory: => SyncAnalyzer[I, O] with SubprocessControls, concurrency: Int) extends AsyncAnalyzer[I, O] {

  val actor: ActorRef = {
    val props = Props(new AnalyzerActor[I, O](factory))
    val realProps = if (concurrency == 1) {
      props
    } else {
      props.withRouter(new RoundRobinPool(concurrency))
    }
    arf.actorOf(realProps)
  }

  override def analyze(input: I)(implicit ec: ExecutionContext): Future[O] = {
    import akka.pattern.ask

    import scala.concurrent.duration._
    implicit val timeout: Timeout = 5.seconds
    (actor ? AnalyzerActor.Request[I](None, input)).map {
      case AnalyzerActor.Reply(_, d) => d.asInstanceOf[O]
      case AnalyzerActor.Failure(_, t) => throw t
    }
  }
}
