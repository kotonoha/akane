package ws.kotonoha.akane.pipe.knp

import ws.kotonoha.akane.pipe.{Analyzer, AbstractRetryExecutor}
import com.typesafe.config.{ConfigFactory, Config}
import ws.kotonoha.akane.config.KnpConfig
import scalax.file.Path
import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import ws.kotonoha.akane.pipe.knp.lisp.{KList, LispParser}
import scala.util.parsing.input.StreamReader
import com.typesafe.scalalogging.slf4j.Logging
import scala.concurrent.ExecutionContext

/**
 * @author eiennohito
 * @since 2013-09-03
 */
class KnpPipeAnalyzer(juman: Process, knp: Process, pipe: String, enc: String) extends Analyzer[Option[KnpNode]] with Logging {

  def close() {
    knp.destroy()
    juman.destroy()
    Path.fromString(pipe).delete(force = true)
  }

  val parser = LispParser.list

  def analyze(in: String) = {
    val input = juman.getOutputStream
    val output = knp.getInputStream

    val writer = new OutputStreamWriter(input, enc)
    val reader = new InputStreamReader(output, enc)

    writer.write(in)
    writer.write("\n")
    writer.flush()

    val rd = new BufferedReader(reader)
    val parseInput = StreamReader.apply(rd)
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
