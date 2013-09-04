package ws.kotonoha.akane.pipe.knp

import ws.kotonoha.akane.config.KnpConfig
import scala.concurrent.{Await, ExecutionContext}
import ws.kotonoha.akane.pipe.NamedPipes
import java.lang.ProcessBuilder.Redirect
import java.io.File
import java.util.concurrent.TimeUnit
import java.util

/**
 * @author eiennohito
 * @since 2013-09-03
 */
class KnpPipeExecutorFactory(config: KnpConfig)(implicit val ec: ExecutionContext = ExecutionContext.global) {

  def winLaunch() = {
    val args = new util.ArrayList[String]()
    args.add("cmd")
    args.add("/c")
    args.add(config.juman.executable.replaceAll("/", "\\\\"))
    config.juman.params.foreach(x => args.add(x.replaceAll("/", "\\\\")))
    args.add("|")
    args.add(config.executable.replaceAll("/", "\\\\"))
    config.params.foreach(x => args.add(x.replaceAll("/", "\\\\")))
    args.add("-sexp")
    val pb = new ProcessBuilder(args)
    val cont = new SingleProcessKnpContainer(pb.start())
    new KnpPipeAnalyzer(cont, config.juman.encoding)
  }

  def launch() = {
    System.getProperty("os.name") match {
      case x if x.contains("Windows") => winLaunch()
      case _ => unixLaunch()
    }
  }

  def unixLaunch() = {
    val juman = config.juman
    val jumanArgs = new util.ArrayList[String]()
    jumanArgs.add(juman.executable)
    juman.params.foreach(jumanArgs.add)
    val jumanEx = new ProcessBuilder(jumanArgs)
    val knpArgs = new util.ArrayList[String]()
    knpArgs.add(config.executable)
    config.params.foreach(knpArgs.add)
    knpArgs.add("-sexp")
    val knpEx = new ProcessBuilder(knpArgs)
    val pipe = NamedPipes.pipe()
    val from = Redirect.from(new File(pipe.name))
    val to = Redirect.to(new File(pipe.name))
    jumanEx.redirectOutput(to)
    knpEx.redirectInput(from)

    val jpf = concurrent.future { jumanEx.start() }
    val kpf = concurrent.future { knpEx.start() }
    val lf = for (jp <- jpf; kp <- kpf) yield
      new KnpPipeAnalyzer(new PipedProcessKnpContaner(jp, kp, pipe), juman.encoding)
    Await.result(lf, concurrent.duration.Duration.create(5, TimeUnit.SECONDS))
  }
}
