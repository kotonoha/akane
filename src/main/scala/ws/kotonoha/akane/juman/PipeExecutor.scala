package ws.kotonoha.akane.juman

import java.io.{BufferedReader, InputStreamReader, Closeable}
import collection.mutable.ListBuffer
import ws.kotonoha.akane.JumanEntry
import java.util
import com.weiglewilczek.slf4s.Logging
import org.apache.commons.io.IOUtils

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
      case e => {
        //retry one time with new process
        val exv = process.exitValue()
        val es = process.getErrorStream
        logger.warn(IOUtils.toString(es, encod))
        logger.warn("Process exited with return value %d".format(exv), e)
        process.destroy()
        process = launch
        parseInner(in)
      }
    }
  }


  private def parseInner(in: String): List[JumanEntry] = {
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
