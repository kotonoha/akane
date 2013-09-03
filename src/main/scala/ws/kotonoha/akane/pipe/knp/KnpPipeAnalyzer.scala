package ws.kotonoha.akane.pipe.knp

import ws.kotonoha.akane.pipe.{Analyzer, AbstractRetryExecutor}
import com.typesafe.config.{ConfigFactory, Config}
import ws.kotonoha.akane.config.KnpConfig
import scalax.file.Path
import java.io.{BufferedReader, InputStreamReader, OutputStreamWriter}
import scala.collection.mutable.ListBuffer

/**
 * @author eiennohito
 * @since 2013-09-03
 */
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

class KnpPipeParser private(factory: () => KnpPipeAnalyzer) extends AbstractRetryExecutor[List[String]](factory)
object KnpPipeParser {
  def apply(config: Config = ConfigFactory.empty()) = {
    val knpConfig = KnpConfig.apply(config)
    val factory = new KnpPipeExecutorFactory(knpConfig)
    new KnpPipeParser(factory.launch)
  }
}
