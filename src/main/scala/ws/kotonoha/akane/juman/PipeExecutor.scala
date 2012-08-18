package ws.kotonoha.akane.juman

import java.io.{BufferedReader, InputStreamReader, Closeable}
import collection.mutable.ListBuffer
import ws.kotonoha.akane.JumanEntry

/**
 * @author eiennohito
 * @since 16.08.12
 */

class PipeExecutor(path: String) extends Closeable {

  private val encod = {
    System.getProperty("sun.desktop") match {
      case "windows" => "shift_jis"
      case _ => "utf-8"
    }
  }

  private var process = launch


  private def launch: Process = {
    val procBldr = new ProcessBuilder(path)
    procBldr.start()
  }

  private val endl: Array[Byte] = Array(10.toByte)

  def parse(in: String) = {
    try {
      parseInner(in)
    } catch {
      case e => {
        //retry one time with new process
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
