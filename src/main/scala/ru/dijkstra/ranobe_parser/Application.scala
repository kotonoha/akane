package ru.dijkstra.ranobe_parser
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
case class JumanEntry(writing: String, reading: String, dictForm: String, spPart: String, comment: String) extends JumanMessage



class JumanConnection(port: Int = 32000) extends Actor {
  val JUMAN_LOAD_STRING = """"C:\Program Files\juman\juman.exe" -b -S -C localhost:""" + port
  val host = "localhost"

  var channel: Channel = null
  var bootstrap : ClientBootstrap = null
  var process : Process = JUMAN_LOAD_STRING.run()
  var sender_ : ActorRef = null

  var busy = false
  var queue : List[String] = Nil

  var currentString : String = null
  var result : List[JumanEntry] = Nil

  private def encodeString(input: String) = new String(input.getBytes(), "Shift_JIS")

  private def buildEntry(input: String) = {
    val tokens = input.split(' ')
    val comment = if (input.count(_ == '"') != 2) "NIL" else input.dropWhile(_ != '"').drop(1).dropRight(2)
    JumanEntry(tokens(0), tokens(1), tokens(2), tokens(3), comment)
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
  def receive = {
    case JumanQuery(in) => {
      if (!busy) {
        busy = true
        currentString = in
        channel.write(encodeString(in))
        sender_ = sender
      } else {
        queue = in :: queue
      }
    }
  }
  override def postStop() {
    channel.close().awaitUninterruptibly
    process.destroy()
    bootstrap.releaseExternalResources()
  }

  class JumanOutputHandler extends SimpleChannelHandler {
    override def messageReceived(ctx: ChannelHandlerContext, e:MessageEvent) {
        import org.eiennohito.stolen_utils.UnicodeUtil.isKanji
        val a =e.getMessage.toString

      if (a.equals("EOS")) {
        sender_ ! result.reverse
        result = Nil
        busy = false
        if (queue != Nil) {
          self ! JumanQuery(queue head)
          queue = queue tail
        }

        return
      }

        if (a.startsWith("200")) return
        if (a.startsWith("@")) return
        //if (a.split(' ').head.forall(!isKanji(_))) return

        //result = a.split(' ').take(2).reduce(_ + "|" + _) :: result
        result = buildEntry(a) :: result
    }
    /*
    override def channelConnected(ctx: ChannelHandlerContext, e:MessageEvent) {
      e.getChannel.write("RUN").awaitUninterruptibly()
    }
    */
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
    val system = ActorSystem("atata")
    try {

    a = system.actorOf(Props(new JumanConnection()), "ja")
    val test = "高校生活を三年間で測れば、一学年二百人として、\"一年生から三年生までで\"、先輩後輩同級生、教師までを全部含め、およそ千人の人間と、生活空間を共にするわけだが、一体その中の何人が、自分にとって意味のある人間なのだろうか、なんて考え始めたら、とても絶望的な答が出てしまうことは、誰だって違いないのだから。"
    val response = a.ask(JumanQuery(test + "\n"))(10000)
    println(test)
    println("---")
    println(Await.result(response, Duration(10, "seconds")))
    } catch {
      case _ => gracefulStop(a,Duration(10, "seconds"))(system)
    }
    system.shutdown()
  }
}

