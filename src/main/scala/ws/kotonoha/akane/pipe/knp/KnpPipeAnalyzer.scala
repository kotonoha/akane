package ws.kotonoha.akane.pipe.knp

import java.io._

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import ws.kotonoha.akane.config.KnpConfig
import ws.kotonoha.akane.parser.{KnpTabFormatParser, OldAngUglyKnpTable}
import ws.kotonoha.akane.pipe.knp.lisp.{KList, LispParser}
import ws.kotonoha.akane.pipe.{AbstractRetryExecutor, Analyzer, Pipe}

import scala.util.parsing.input.CharSequenceReader

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

trait KnpResultParser {
  type Result
  def parse(reader: BufferedReader): Result
}

class KnpPipeAnalyzer[RParser <: KnpResultParser](cont: KnpProcessContainer, enc: String, parser: RParser) extends Analyzer[RParser#Result] with StrictLogging {

  def close() {
    cont.close()
  }

  def analyze(in: String): RParser#Result = {
    val writer = new OutputStreamWriter(cont.output, enc)
    val reader = new InputStreamReader(cont.input, enc)

    writer.write(in)
    writer.write("\n")
    writer.flush()

    val rd = new BufferedReader(reader)
    parser.parse(rd)
  }
}


class SexpKnpResultParser extends KnpResultParser with StrictLogging {
  override type Result = Option[KnpNode]

  val parser = LispParser.list

  override def parse(rd: BufferedReader) = {
    val stringBuilder = new StringBuilder
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
    lisp.flatMap(KnpSexpParser.parseTree)
  }
}

class KnpTreePipeParser private(factory: () => KnpPipeAnalyzer[SexpKnpResultParser]) extends AbstractRetryExecutor[Option[KnpNode]](factory)
object KnpTreePipeParser {
  def apply(config: Config = ConfigFactory.empty()) = {
    val knpConfig = KnpConfig.apply(config)
    val factory = new KnpProcessFactory(knpConfig, KnpOutputType.sexp)
    new KnpTreePipeParser(() => new KnpPipeAnalyzer(factory.launch(), knpConfig.juman.encoding, new SexpKnpResultParser))
  }
}

class KnpTabPipeParser private(factory: () => KnpPipeAnalyzer[KnpTabFormatParser]) extends AbstractRetryExecutor[Option[OldAngUglyKnpTable]](factory)
object KnpTabPipeParser {
  def apply(config: Config = ConfigFactory.empty()) = {
    val knpConfig = KnpConfig.apply(config)
    val factory = new KnpProcessFactory(knpConfig, KnpOutputType.tab)
    new KnpTabPipeParser(() => new KnpPipeAnalyzer(factory.launch(), knpConfig.juman.encoding, new KnpTabFormatParser))
  }
}
