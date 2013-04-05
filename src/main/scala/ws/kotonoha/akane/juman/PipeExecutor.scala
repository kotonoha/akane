package ws.kotonoha.akane.juman

import java.io.{BufferedReader, InputStreamReader, Closeable}
import collection.mutable.ListBuffer
import ws.kotonoha.akane.JumanEntry
import java.util
import org.apache.commons.io.IOUtils
import com.typesafe.scalalogging.slf4j.Logging

/**
 * @author eiennohito
 * @since 16.08.12
 */

class PipeExecutor(path: String, args: List[String] = Nil, encoding: Option[String] = None) extends Closeable with Logging {

  private val encod = {
    encoding getOrElse (System.getProperty("sun.desktop") match {
      case "windows" => "shift_jis"
      case _ => "utf-8"
    })
  }

  private var process = launch


  private def launch: Process = {
    val lst = new util.ArrayList[String]()
    lst.add(path)
    args.foreach(lst.add(_))
    val procBldr = new ProcessBuilder(lst)
    procBldr.start()
  }

  private val endl: Array[Byte] = Array(10.toByte)

  def parse(in: String) = {
    try {
      parseInner(in)
    } catch {
      case e: Throwable => {
        //retry one time with new process
        val exv = process.exitValue()
        val es = process.getErrorStream
        logger.warn(IOUtils.toString(es, encod))
        logger.warn(s"Process exited with return value $exv", e)
        process.destroy()
        process = launch
        parseInner(in)
      }
    }
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


  private def parseInner(raw: String): List[JumanEntry] = {
    val in = sanitize(raw)
    val os = process.getOutputStream
    os.write(in.getBytes(encod))
    if (!in.endsWith("\n")) {
      os.write(endl)
    }
    os.flush()
    val rdr = new InputStreamReader(process.getInputStream, encod)
    val buf = new BufferedReader(rdr)
    var ok = true
    val bldr = new ListBuffer[JumanEntry]
    do {
      val line = buf.readLine()
      line match {
        case "EOS" => ok = false
        case x if x.startsWith("@") => //ignore
        case x => bldr += JumanEntry.parse(line)
      }
    } while (ok)
    bldr.toList
  }

  def close() {
    process.destroy()
  }
}
