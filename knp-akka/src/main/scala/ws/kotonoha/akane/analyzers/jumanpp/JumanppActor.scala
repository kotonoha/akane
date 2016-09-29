package ws.kotonoha.akane.analyzers.jumanpp

import akka.actor.Actor
import com.typesafe.config.Config
import ws.kotonoha.akane.analyzers.jumanpp.wire.Lattice

import scala.util.{Failure, Success}

/**
  * @author eiennohito
  * @since 2016/09/28
  */
class JumanppActor(conf: Config) extends Actor {
  import JumanppActor._

  lazy val process = JumanppSubprocess.create(JumanppConfig(conf))

  override def receive = {
    case AnalyzeRequest(ref, data) =>
      val res = process.analyzeSync(data)
      res match {
        case Success(seq) => sender() ! AnalysisSuccess(ref, seq)
        case Failure(x) => sender() ! AnalysisFailure(ref, x)
      }
    case RestartSubprocess =>
      process.restart()
  }

  @throws[Exception](classOf[Exception])
  override def postStop() = {
    super.postStop()
    process.close()
  }

}

object JumanppActor {
  trait Message
  case class AnalyzeRequest(ref: AnyRef, input: String) extends Message
  case object RestartSubprocess extends Message

  case class AnalysisSuccess(ref: AnyRef, result: Lattice)
  case class AnalysisFailure(ref: AnyRef, error: Throwable)
}
