package ru.dijkstra.ranobe_parser.runner

import scalax.file.Path
import java.io.{PrintWriter, InputStreamReader}
import ru.dijkstra.ranobe_parser.parser.{DebugInput, AozoraParser, StreamReaderInput}
import ru.dijkstra.ranobe_parser.juman.PipeExecutor
import ru.dijkstra.ranobe_parser.statistics.FrequencyAnalyzer
import scalax.io.Codec
import collection.mutable

/**
 * @author eiennohito
 * @since 18.08.12
 */

object JumanFrequencyAnalyzer {
  def main(args: Array[String]) = {
    val path = Path(args(0))
    val pex = new PipeExecutor("juman.exe")
    val enc = args(1)
    val ignore = args.slice(2, args.length).foldLeft (new mutable.HashSet[String]()) {case (hs, path) => {
      val p = Path(path)
      p.lines()(Codec.UTF8).filter(!_.startsWith("#")).foreach { hs += _ }
      hs
    }}.toSet[String]
    for (is <- path.inputStream()) {
      val isr = new InputStreamReader(is, enc)
      val inp = new StreamReaderInput(isr)
      val jp = new AozoraParser(inp)
      val fa = new FrequencyAnalyzer(pex, ignore)
      val info = fa.analyze(jp)
      val pw = new PrintWriter("e:/temp/bake_nfo.txt")
      pw.println("total: %d results".format(info.cnt))
      info.items.foreach{ i => pw.println("%s -> %d".format(i.item, i.cnt)) }
      pw.close()
    }
  }
}
