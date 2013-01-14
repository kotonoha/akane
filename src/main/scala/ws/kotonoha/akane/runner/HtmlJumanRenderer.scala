package ws.kotonoha.akane.runner

import ws.kotonoha.akane.parser.{AozoraParser, StreamReaderInput}
import scalax.file.Path
import java.io.{PrintWriter, InputStreamReader}
import ws.kotonoha.akane.transform.JumanTransformer
import ws.kotonoha.akane.juman.PipeExecutor
import ws.kotonoha.akane.ast.Sentence
import ws.kotonoha.akane.render.HtmlRenderer
import xml.{MinimizeMode, Utility}

/**
 * @author eiennohito
 * @since 17.08.12
 */

object HtmlJumanRenderer {
  def main(args: Array[String]) = {
    val fn = args(0)
    val res = Path.fromString(fn) inputStream
    val pe = new PipeExecutor("juman.exe")
    val jt = new JumanTransformer(pe)
    for (is <- res) {
      val rdr = new InputStreamReader(is, args(1))
      val inp = new StreamReaderInput(rdr)
      val parser = new AozoraParser(inp)
      val hr = new HtmlRenderer
      val sb = new StringBuilder(8 * 1024)
      val parsed = parser.toArray
      val nodes = parsed map {
        case s: Sentence => jt.transformSentence(s)
        case n => n
      } map {
        hr.render(_)
      }
      val out = Path.fromString(args(0) + ".html")

      val doc =
        <html>
          <head>
          </head>
          <body>
            {nodes.toList}
          </body>
        </html>

      val pw = new PrintWriter(out.path, "utf-8")

      sb.clear()
      Utility.sequenceToXML(doc, sb = sb, minimizeTags = MinimizeMode.Always)
      println(sb.toString())
      pw.println(sb.toString())

      pw.close()

    }

  }
}
