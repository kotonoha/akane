package ru.dijkstra.ranobe_parser.runner

import ru.dijkstra.ranobe_parser.parser.{AozoraParser, StreamReaderInput}
import scalax.file.Path
import java.io.{PrintWriter, InputStreamReader}
import ru.dijkstra.ranobe_parser.transform.JumanTransformer
import ru.dijkstra.ranobe_parser.juman.PipeExecutor
import ru.dijkstra.ranobe_parser.ast.Sentence
import ru.dijkstra.ranobe_parser.render.HtmlRenderer
import xml.Utility

/**
 * @author eiennohito
 * @since 17.08.12
 */

object HtmlJumanRenderer {
  def main(args: Array[String]) = {
    val fn = args(0)
    val res = Path(fn) inputStream()
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
      val out = Path(args(0) + ".html")

      val pw = new PrintWriter(out.path, "utf-8")
      nodes foreach {n => {
        sb.clear()
        Utility.sequenceToXML(n, sb = sb, minimizeTags = true)
        println(sb.toString())
        pw.println(sb.toString())
      }}
      pw.close()

    }

  }
}
