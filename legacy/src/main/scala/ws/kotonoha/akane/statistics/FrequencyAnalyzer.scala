/*
 * Copyright 2012-2016 eiennohito (Tolmachev Arseny)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ws.kotonoha.akane.statistics

import ws.kotonoha.akane.ast.{HighLvlNode, Node, Sentence}
import ws.kotonoha.akane.juman.{JumanPipeExecutor, JumanUtil}
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

class FrequencyAnalyzer(juman: JumanPipeExecutor, stoplist: Set[String] = Set()) {
  val msr = new MetaStringRenderer()

  def processSentence(node: Node, cnts: mutable.Map[String, Int]): Int = {
    val rndred = msr.render(node)
    var i = 0
    juman
      .parse(rndred.data)
      .foreach(je => {
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
    in.foreach {
      case Sentence(s) => total += processSentence(s, map.withDefaultValue(0))
      case _           => //ignore
    }
    val arr = map.map { case (i1, i2) => FreqItem(i1, i2) }.toList
    FrequencyInfo(total, arr.sortBy(-_.cnt))
  }
}
