package ws.kotonoha.akane.statistics

import ws.kotonoha.akane.ast.{Node, Sentence, HighLvlNode}
import ws.kotonoha.akane.juman.{JumanUtil, JumanPipeExecutor}
import ws.kotonoha.akane.pipe.juman.JumanEntry
import collection.mutable
import ws.kotonoha.akane.render.MetaStringRenderer
import util.matching.Regex.{Groups, Match}
import util.matching.Regex

/**
 * @author eiennohito
 * @since 18.08.12
 */
case class FreqItem(item: String, cnt: Int)
case class FrequencyInfo(cnt: Int, items: List[FreqItem])



class FrequencyAnalyzer (juman: JumanPipeExecutor, stoplist: Set[String] = Set()) {
  val msr = new MetaStringRenderer()

  def processSentence(node: Node, cnts: mutable.Map[String, Int]): Int = {
    val rndred = msr.render(node)
    var i = 0
    juman.parse(rndred.data) foreach (je => {
      val key = JumanUtil.daihyouWriting(je).writing
      if (!stoplist.contains(key) && JumanUtil.ignored(key)) {
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
