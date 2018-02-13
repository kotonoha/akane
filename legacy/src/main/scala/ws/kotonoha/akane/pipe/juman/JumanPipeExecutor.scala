/* Copyright */
package ws.kotonoha.akane.juman

import java.io.{BufferedReader, InputStreamReader}

import com.typesafe.scalalogging.StrictLogging
import ws.kotonoha.akane.pipe.juman.JumanEntry

import collection.mutable.ListBuffer
import org.apache.commons.io.IOUtils
import ws.kotonoha.akane.pipe.{AbstractRetryExecutor, Analyzer}
import com.typesafe.config.{Config, ConfigFactory}
import java.util

import ws.kotonoha.akane.analyzers.juman.JumanConfig

class ProcessException(msg: String, errCode: Int, inner: Throwable)
    extends RuntimeException(msg, inner)

trait ProcessSupport {
  def withProcess[T](p: Process, enc: String)(f: => T): T =
    try {
      f
    } catch {
      case e: Throwable =>
        val exv = p.exitValue()
        val es = p.getErrorStream
        val s = IOUtils.toString(es, enc)
        val msg = s"Error with process, retcode = $exv\n$s"
        throw new ProcessException(msg, exv, e)
    }
}

/**
  * @author eiennohito
  * @since 16.08.12
  */
class JumanPipeAnalyzer(process: Process, encoding: String)
    extends Analyzer[List[JumanEntry]]
    with StrictLogging
    with ProcessSupport {

  def analyze(input: String) = withProcess(process, encoding) { parseInner(input) }

  def close() {
    process.destroy()
  }

  def sanitize(s: String) = {
    val buf = new StringBuilder(s.length)
    val len = s.length
    var i = 0
    while (i < len) {
      val c = s(i)
      if (c >= 0x20)
        buf.append(c)
      i += 1
    }
    buf.toString()
  }

  val endl = Array(10.toByte)

  private def parseInner(raw: String): List[JumanEntry] = {
    val in = sanitize(raw)
    val os = process.getOutputStream
    os.write(in.getBytes(encoding))
    if (!in.endsWith("\n")) {
      os.write(endl)
    }
    os.flush()
    val rdr = new InputStreamReader(process.getInputStream, encoding)
    val buf = new BufferedReader(rdr)
    var ok = true
    val bldr = new ListBuffer[JumanEntry]
    do {
      val line = buf.readLine()
      line match {
        case "EOS"                  => ok = false
        case x if x.startsWith("@") =>
        case x                      => bldr += JumanEntry.parse(line)
      }
    } while (ok)
    bldr.toList
  }
}

class JumanPipeExecutor private (fact: () => JumanPipeAnalyzer)
    extends AbstractRetryExecutor[List[JumanEntry]](fact) {}

object JumanPipeExecutor {
  def apply(config: Config = ConfigFactory.empty()) = {
    val jconf = JumanConfig(config)
    new JumanPipeExecutor(() => launch(jconf))
  }

  def launch(config: JumanConfig) = {
    val lst = new util.ArrayList[String]()
    lst.add(config.executable)
    config.params.foreach(lst.add)
    val procBldr = new ProcessBuilder(lst)
    val proc = procBldr.start()
    new JumanPipeAnalyzer(proc, config.encoding)
  }
}
