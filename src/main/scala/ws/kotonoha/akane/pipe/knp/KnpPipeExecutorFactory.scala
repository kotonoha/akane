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
class KnpPipeExecutorFactory(config: KnpConfig) {
  implicit val ec: ExecutionContext = ExecutionContext.global
  def launch() = {
    val juman = config.juman
    val jumanArgs = new util.ArrayList[String]()
    jumanArgs.add(juman.executable)
    juman.params.foreach(jumanArgs.add)
    val jumanEx = new ProcessBuilder(jumanArgs)
    val knpArgs = new util.ArrayList[String]()
    knpArgs.add(config.executable)
    config.params.foreach(knpArgs.add)
    knpArgs.add("-mrphtab")
    val knpEx = new ProcessBuilder(knpArgs)
    val pipe = NamedPipes.pipe()
    val from = Redirect.from(new File(pipe))
    val to = Redirect.to(new File(pipe))
    jumanEx.redirectOutput(to)
    knpEx.redirectInput(from)

    val jpf = concurrent.future { jumanEx.start() }
    val kpf = concurrent.future { knpEx.start() }
    val lf = for (jp <- jpf; kp <- kpf) yield
      new KnpPipeAnalyzer(jp, kp, pipe, juman.encoding)
    Await.result(lf, concurrent.duration.Duration.create(5, TimeUnit.SECONDS))
  }
}
