package ws.kotonoha.akane
import akka.actor._
import annotation.tailrec



class TokenizerActor(pipeOutput: Actor) extends Actor{


  protected def receive = {
    case ProcessLine(line) => {

    }
  }
}
