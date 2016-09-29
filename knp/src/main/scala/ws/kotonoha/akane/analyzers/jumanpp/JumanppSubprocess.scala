package ws.kotonoha.akane.analyzers.jumanpp

import java.io.{BufferedReader, InputStream, InputStreamReader, OutputStream}
import java.nio.{ByteBuffer, CharBuffer}
import java.util

import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import ws.kotonoha.akane.analyzers.jumanpp.wire.Lattice
import ws.kotonoha.akane.analyzers._
import ws.kotonoha.akane.config.AkaneConfig
import ws.kotonoha.akane.io.Charsets

import scala.collection.JavaConverters._
import scala.util.Try

/**
  * @author eiennohito
  * @since 2016/09/28
  */

trait JumanppAnalyzer extends SyncAnalyzer[String, Lattice]

object JumanppSubprocess extends StrictLogging {
  def create(cfg: JumanppConfig): JumanppAnalyzer with SubprocessControls = new JumanppSubprocessImpl(cfg)

  private def process(cfg: JumanppConfig): Process = {
    val commands = new util.ArrayList[String]()
    commands.add(cfg.executable)
    cfg.resources.foreach { jc =>
      commands.add("--dir")
      commands.add(jc)
    }

    commands.add("--specifics")
    commands.add(cfg.latticeSize.toString)

    commands.addAll(cfg.otherArgs.asJava)

    val pb = new ProcessBuilder(commands)
    val process = pb.start()

    logger.debug(s"starting juman++ using ${cfg.executable} with lattice of ${cfg.latticeSize}: isalive = ${process.isAlive}")

    process
  }

  final class JumanppOutput extends FromStream[Lattice] {
    private val parser = new JppLatticeParser

    override def readFrom(s: InputStream): Try[Lattice] = {
      Try(parser.parse(new BufferedReader(new InputStreamReader(s, Charsets.utf8))))
    }
  }

  final class JumanppInput extends ToStream[String] {
    private val eol = '\n'.toByte

    private val inputBuffer = CharBuffer.allocate(2 * 1024)
    private val utf8Buffer = ByteBuffer.allocate(8 * 1024) //will always fit
    private val encoder = Charsets.utf8.newEncoder()

    override def writeTo(s: OutputStream, inp: String): Unit = {
      inputBuffer.clear()
      var i = 0
      while (i < inp.length) {
        val c = inp(i)
        c match {
          case '\t' => inputBuffer.put(' ')
          case _ => inputBuffer.put(c)
        }
        i += 1
      }
      inputBuffer.flip()
      utf8Buffer.clear()
      val result = encoder.encode(inputBuffer, utf8Buffer, true)
      if (result.isError) {
        result.throwException()
      }
      utf8Buffer.put(eol)
      utf8Buffer.flip()
      s.write(utf8Buffer.array(), 0, utf8Buffer.remaining())
    }
  }

  private class JumanppSubprocessImpl(cfg: JumanppConfig)
    extends SpawnedProcessAnalyzer[String, Lattice](process(cfg))(new JumanppInput, new JumanppOutput)
    with JumanppAnalyzer {

  }

}

case class JumanppConfig (
  executable: String,
  resources: Option[String] = None,
  latticeSize: Int = 1,
  otherArgs: Seq[String] = Nil
)

object JumanppConfig {
  import ws.kotonoha.akane.config.ScalaConfig._
  def apply(cfg: Config): JumanppConfig = {
    val mycfg = cfg.withFallback(AkaneConfig.default).getConfig("akane.jumanpp")
    val exec = mycfg.optStr("executable").getOrElse("jumanpp")
    val configFile = mycfg.optStr("resources")
    val latticeSize = mycfg.intOr("lattice", 1)
    val otherArgs = mycfg.getStringList("args")
    JumanppConfig(exec, configFile, latticeSize, otherArgs.asScala)
  }

  lazy val default = JumanppConfig.apply(AkaneConfig.default)
}
