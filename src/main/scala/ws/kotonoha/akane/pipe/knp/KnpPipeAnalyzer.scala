package ws.kotonoha.akane.pipe.knp

import ws.kotonoha.akane.pipe.{Pipe, Analyzer, AbstractRetryExecutor}
import com.typesafe.config.{ConfigFactory, Config}
import ws.kotonoha.akane.config.KnpConfig
import scalax.file.Path
import java.io._
import ws.kotonoha.akane.pipe.knp.lisp.{KList, LispParser}
import scala.util.parsing.input.{CharSequenceReader, StreamReader}
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.ExecutionContext
import ws.kotonoha.akane.pipe.knp.lisp.KList
import scala.Some
import ws.kotonoha.akane.pipe.knp.KnpNode

/**
 * @author eiennohito
 * @since 2013-09-03
 */

trait KnpProcessContainer extends Closeable {
  def output: OutputStream
  def input: InputStream
}

class SingleProcessKnpContainer(process: Process) extends KnpProcessContainer {
  def output = process.getOutputStream

  def input = process.getInputStream

  def close() {
    process.destroy()
  }
}

class PipedProcessKnpContaner(juman: Process, knp: Process, pipe: Pipe) extends KnpProcessContainer {
  def input = knp.getInputStream

  def output = juman.getOutputStream

  def close() {
    knp.destroy()
    juman.destroy()
    pipe.close()
  }
}

class KnpPipeAnalyzer(cont: KnpProcessContainer, enc: String) extends Analyzer[Option[KnpNode]] with Logging {

  def close() {
    cont.close()
  }

  val parser = LispParser.list

  def analyze(in: String) = {
    val writer = new OutputStreamWriter(cont.output, enc)
    val reader = new InputStreamReader(cont.input, enc)

    writer.write(in)
    writer.write("\n")
    writer.flush()

    val stringBuilder = new StringBuilder
    val rd = new BufferedReader(reader)
    var continue = true
    do {
      val line = rd.readLine()
      if (line == "EOS") {
        continue = false
      }
      stringBuilder.append(line).append("\n")
    } while(continue)

    val parseInput = new CharSequenceReader(stringBuilder)
    val lisp = parser(parseInput) match {
      case LispParser.Success(res, _) => Some(res.asInstanceOf[KList])
      case x => logger.warn("can't parse knp output " + x); None
    }
    lisp.flatMap(KnpParser.parseTree)
  }
}

class KnpPipeParser private(factory: () => KnpPipeAnalyzer) extends AbstractRetryExecutor[Option[KnpNode]](factory)
object KnpPipeParser {
  def apply(config: Config = ConfigFactory.empty())(implicit ec: ExecutionContext) = {
    val knpConfig = KnpConfig.apply(config)
    val factory = new KnpPipeExecutorFactory(knpConfig)
    new KnpPipeParser(factory.launch)
  }
}
