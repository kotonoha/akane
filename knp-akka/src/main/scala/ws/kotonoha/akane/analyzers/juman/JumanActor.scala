/*
 * Copyright 2012-2016 eiennohito (Tolmachev Arseny)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ws.kotonoha.akane.analyzers.juman

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

/**
  * @author eiennohito
  * @since 15/10/02
  */
class JumanActor(conf: JumanConfig) extends Actor {
  lazy val process = JumanSubprocess.create(conf)

  import JumanActor._

  override def receive = {
    case AnalyzeRequest(ref, data) =>
      val res = process.analyzeSync(data)
      res match {
        case Success(seq) => sender() ! AnalysisSuccess(ref, seq)
        case Failure(x)   => sender() ! AnalysisFailure(ref)
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

object JumanActor {
  def props(conf: JumanConfig): Props = Props(new JumanActor(conf))

  trait Message
  case class AnalyzeRequest(ref: AnyRef, data: String) extends Message
  sealed trait AnalysisResult extends Message {
    def ref: AnyRef
  }
  case class AnalysisFailure(ref: AnyRef) extends AnalysisResult
  case class AnalysisSuccess(ref: AnyRef, seq: JumanSentence) extends AnalysisResult
  case object RestartSubprocess
}

class JumanActorAnalyzer(ref: ActorRef, timeout: Timeout) extends AsyncJumanAnalyzer {

  import akka.pattern.ask

  override def analyze(input: String)(implicit ec: ExecutionContext) = {
    ref
      .ask(JumanActor.AnalyzeRequest(null, input))(timeout)
      .mapTo[JumanActor.AnalysisResult]
      .flatMap {
        case JumanActor.AnalysisSuccess(_, seq) => Future.successful(seq)
        case JumanActor.AnalysisFailure(_) =>
          Future.failed(throw new Exception(s"could not parse $input"))
      }
  }
}
