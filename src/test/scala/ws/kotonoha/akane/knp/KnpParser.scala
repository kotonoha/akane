package ws.kotonoha.akane.knp

import java.io._
import java.util
import scala.collection.mutable.ListBuffer
import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers
import scalax.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.lang.ProcessBuilder.Redirect
import scala.concurrent.{Await, ExecutionContext}
import java.util.concurrent.TimeUnit
import ws.kotonoha.akane.config.KnpConfig
import ws.kotonoha.akane.pipe.{AbstractRetryExecutor, Analyzer}
import com.typesafe.config.{ConfigFactory, Config}

/**
 * @author eiennohito
 * @since 2013-08-12
 */
class KnpParser {

}

class KnpPipeAnalyzer(juman: Process, knp: Process, pipe: String, enc: String) extends Analyzer[List[String]] {

  def close() {
    knp.destroy()
    juman.destroy()
    Path.fromString(pipe).delete(force = true)
  }

  def analyze(in: String) = {
    val input = juman.getOutputStream
    val output = knp.getInputStream

    val writer = new OutputStreamWriter(input, enc)
    val reader = new InputStreamReader(output, enc)

    writer.write(in)
    writer.write("\n")
    writer.flush()

    val rd = new BufferedReader(reader)
    val lines = new ListBuffer[String]
    var ok = true
    do {
      val line = rd.readLine()
      line match {
        case "EOS" => ok = false
        case x if x.startsWith("#") => //skip comment
        case x => lines += x
      }
    } while (ok)
    lines.result()
  }
}

object NamedPipes {
  val directory = Path.fromString("/tmp/kotonoha/pipes/")
  val counter = new AtomicInteger(directory.children().count(_ => true) + 1)

  def pipe() = {
    if (directory.nonExistent)
      directory.createDirectory(createParents = true)
    val path = directory / counter.addAndGet(1).toString
    val proc = new ProcessBuilder("/usr/bin/mkfifo", path.path).start()
    if (proc.waitFor() == 0)
      path.path
    else throw new RuntimeException("could not create named pipe")
  }
}

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

class KnpPipeParser private(factory: () => KnpPipeAnalyzer) extends AbstractRetryExecutor[List[String]](factory)
object KnpPipeParser {
  def apply(config: Config = ConfigFactory.empty()) = {
    val knpConfig = KnpConfig.apply(config)
    val factory = new KnpPipeExecutorFactory(knpConfig)
    new KnpPipeParser(factory.launch)
  }
}


class KnpExecutorText extends FreeSpec with ShouldMatchers {
  "knp executor" - {
    "works" in {
      val knp = KnpPipeParser()
      val lines = knp.parse("私は何も知りません")
      lines.foreach(println)
      knp.close()
    }
  }
}


//かわったり かわったり かわる 動詞 2 * 0 子音動詞ラ行 10 タ系連用タリ形 15 "代表表記:代わる/かわる 自他動詞:他:代える/かえる"
case class KnpLexeme(
surface: String,
reading: String,
dicForm: String,
pos: String,
fld1: Int,
fld2: Option[String],
fld3: Int,
posType: String,
fld4: Int,
form: String,
fld5: Int,
info: String,
tags: List[String]
                      )

case class KnpItem(num: Int, star: String, plus: String, lexems: Seq[KnpLexeme])

object KnpParser {
  def parseTab(lines: Seq[String]) = {
    val buffer = new collection.mutable.ArrayBuffer[KnpItem]()
  }
}