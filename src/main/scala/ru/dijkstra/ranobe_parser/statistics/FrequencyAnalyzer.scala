package ru.dijkstra.ranobe_parser.statistics

import ru.dijkstra.ranobe_parser.ast.{Node, Sentence, HighLvlNode}
import ru.dijkstra.ranobe_parser.juman.PipeExecutor
import collection.mutable
import ru.dijkstra.ranobe_parser.render.MetaStringRenderer
import ru.dijkstra.ranobe_parser.JumanEntry
import util.matching.Regex.{Groups, Match}
import util.matching.Regex

/**
 * @author eiennohito
 * @since 18.08.12
 */
case class FreqItem(item: String, cnt: Int)
case class FrequencyInfo(cnt: Int, items: List[FreqItem])

class FrequencyAnalyzer (juman: PipeExecutor, stoplist: Set[String] = Set()) {

  val regex = """代表表記:(.+?)/(.+?)""".r

  val ignore = """^[- 　0-9０-９a-zA-Zａ-ｚＡ-Ｚ。、（）〈〉()\[\]……~～〜\\＝/"・?？!！——「」『』]+$""".r

  def stripDa(str: String, pos: String): String = {
    if (pos.contains("形容") || pos.equals("助動詞")) {
      if (str.endsWith("だ")) {
        return str.substring(0, str.length - 1)
      }
    }
    str
  }

  def daihyouWriting(ent: JumanEntry) = {
    regex.findFirstMatchIn(ent.comment) match {
      case Some(Groups(wr, _)) => stripDa(wr, ent.spPart)
      case _ => stripDa(ent.dictForm, ent.spPart)
    }
  }

  val msr = new MetaStringRenderer()

  def processSentence(node: Node, cnts: mutable.Map[String, Int]): Int = {
    val rndred = msr.render(node)
    var i = 0
    juman.parse(rndred.data) foreach (je => {
      val key = daihyouWriting(je)
      if (!stoplist.contains(key) && ignore.findFirstIn(key).isEmpty) {
        i += 1
        cnts(key) = cnts(key) + 1
      }
    })
    i
  }

  def analyze(in: TraversableOnce[HighLvlNode]) = {
    val map = new mutable.HashMap[String, Int]
    var total = 0
    in foreach {
      case Sentence(s) => total += processSentence(s, map.withDefaultValue(0))
      case _ => //ignore
    }
    val arr = map.map {case (i1, i2) => FreqItem(i1, i2)}.toList
    FrequencyInfo(total, arr.sortBy(-_.cnt))
  }
}
