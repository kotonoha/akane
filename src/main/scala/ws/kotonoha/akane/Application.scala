package ws.kotonoha.akane
import akka.actor._
import akka.pattern._
import org.jboss.netty.channel._
import org.jboss.netty.handler.codec.frame.{Delimiters, DelimiterBasedFrameDecoder}
import org.jboss.netty.handler.codec.string.{StringEncoder, StringDecoder}
import org.jboss.netty.bootstrap.ClientBootstrap
import socket.nio.NioClientSocketChannelFactory
import java.util.concurrent.Executors
import java.net.InetSocketAddress
import akka.dispatch.Await
import akka.util.Duration
import sys.process._

sealed trait JumanMessage
case class JumanQuery(input: String) extends JumanMessage
case class DelayedJumanQuery(input: String, returnAdress: ActorRef) extends JumanMessage
case class JumanEntry(writing: String, reading: String, dictForm: String, spPart: String, comment: String) extends JumanMessage
case class ParsedQuery(inner : List[JumanEntry]) extends JumanMessage

object JumanEntry {
  def parse(in: String) = {
    val tokens = in.split(' ')
    //val comment = if (in.count(_ == '"') != 2) "NIL" else in.dropWhile(_ != '"').drop(1).dropRight(2)
    val left = in.indexOf('\"')
    val comment = {
      if (left == -1) {
        "NIL"
      } else {
        in.substring(left + 1, in.indexOf('\"', left + 1))
      }
    }
    JumanEntry(tokens(0), tokens(1), tokens(2), tokens(3), comment)
  }
}

object JumanRW {
  def unapply(in: JumanEntry) = Some((in.writing, in.reading))
}

class StupidPrintingPipeOutputActor(system: ActorSystem) extends Actor {
  def receive = {
    case a => {
      println(a)
      system.shutdown()
    }
  }
}

class JumanConnectionActor(outputPipe : ActorRef, port: Int = 32000) extends Actor {
  val JUMAN_LOAD_STRING = """"C:\Program Files\juman\juman.exe" -b -S -C localhost:""" + port
  val host = "localhost"

  var channel: Channel = null
  var bootstrap : ClientBootstrap = null
  var process : Process = JUMAN_LOAD_STRING.run()

  private def encodeString(input: String) = new String(input.getBytes(), "Shift_JIS")
  private def buildEntry(input: String) = {
    JumanEntry.parse(input)
  }

  override def preStart() {
    bootstrap = new ClientBootstrap(
      new NioClientSocketChannelFactory(Executors.newCachedThreadPool, Executors.newCachedThreadPool))
    bootstrap.setPipelineFactory(new TelnetClientPipelineFactory)
    val future: ChannelFuture = bootstrap.connect(new InetSocketAddress(host, port))
    channel = future.awaitUninterruptibly.getChannel
    if (!future.isSuccess) {
      future.getCause.printStackTrace()
      bootstrap.releaseExternalResources()
      throw new Exception("Error: Cannot establish connection")
    }
    channel.write("RUN\n").awaitUninterruptibly()
  }
  override def postStop() {
    channel.close().awaitUninterruptibly
    process.destroy()
    bootstrap.releaseExternalResources()
  }

  var busy = false
  var queue : List[String] = Nil
  def receive = {
    case JumanQuery(in) => {
      if (!busy) {
        busy = true
        channel.write(encodeString(in) + '\n')
      } else {
        queue = in :: queue
      }
    }
    case result@ParsedQuery(_) => {
      if (!busy) throw new Exception("Error: unexpected response")
      outputPipe ! result
      if (!queue.isEmpty) {
        channel.write(encodeString(queue.head) + '\n')
        queue = queue tail
      } else busy = false
    }
  }

  class JumanOutputHandler extends SimpleChannelHandler {
    var result : List[JumanEntry] = Nil
    override def messageReceived(ctx: ChannelHandlerContext, e:MessageEvent) {
      val a =e.getMessage.toString
      if (a.equals("EOS")) {
        self ! ParsedQuery(result.reverse)
        result = Nil
        return
      }
      if (a.startsWith("200")) return
      if (a.startsWith("@")) return
      result = buildEntry(a) :: result
    }
    override def exceptionCaught(context: ChannelHandlerContext, e: ExceptionEvent) {
      e.getChannel.close()
    }
  }
  class TelnetClientPipelineFactory extends ChannelPipelineFactory {
    override def getPipeline = {
      val pipeline = org.jboss.netty.channel.Channels.pipeline
      pipeline.addLast("framer", new DelimiterBasedFrameDecoder(
        8192, (Delimiters.lineDelimiter): _*))
      pipeline.addLast("decoder", new StringDecoder)
      pipeline.addLast("encoder", new StringEncoder)
      pipeline.addLast("handler", new JumanOutputHandler)
      pipeline
    }
}
}

object Application {
  def main(args: Array[String]): Unit = {
    var a: ActorRef = null
    val system: ActorSystem = ActorSystem("atata")
    val b = system.actorOf(Props(new StupidPrintingPipeOutputActor(system)), "printer")
    a = system.actorOf(Props(new JumanConnectionActor(b)), "juman_interoper")
    val test = "高校生活を三年間で測れば、一学年二百人として、\"一年生から三年生までで\"、先輩後輩同級生、教師までを全部含め、およそ千人の人間と、生活空間を共にするわけだが、一体その中の何人が、自分にとって意味のある人間なのだろうか、なんて考え始めたら、とても絶望的な答が出てしまうことは、誰だって違いないのだから。"
    println(test)
    println("---")
    a ! JumanQuery(test)


  }
}

