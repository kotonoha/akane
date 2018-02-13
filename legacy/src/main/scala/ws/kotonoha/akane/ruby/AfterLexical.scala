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

package ws.kotonoha.akane.ruby

import ws.kotonoha.akane.ast.{ListNode, Node, RubyNode, StringNode}
import collection.mutable.ListBuffer

/**
  * @author eiennohito
  * @since 15.08.12
  */
object AfterLexical {

  def matchFront(wr: String, rd: String) = {
    var i = 0
    val wlen = wr.length
    val rlen = rd.length
    while (i < wlen && i < rlen && wr(i) == rd(i)) {
      i += 1
    }
    i
  }

  def matchBack(wr: String, rd: String) = {
    var i = 0
    val wl = wr.length - 1
    val rl = rd.length - 1
    val max = math.min(wl, rl)
    while (i < max && wr(wl - i) == rd(rl - i)) {
      i += 1
    }
    i
  }

  def matchMid(wr: String, rd: String): Node = {
    RubyNode(rd, StringNode(wr))
  }

  /**
    * Makes node from parsed values
    * @param writing - writing (as from lexical analyzer)
    * @param reading - reading (as from lexical analyzer)
    * @return
    */
  def makeNode(writing: String, reading: String): Node = {
    if (writing.equals(reading)) {
      return StringNode(reading)
    }
    val nodes = ListBuffer[Node]()
    val fr = matchFront(writing, reading)
    if (fr != 0) {
      nodes += StringNode(reading.substring(0, fr))
    }
    val bck = matchBack(writing.substring(fr), reading.substring(fr))
    val wbl = writing.length - bck
    val rbl = reading.length - bck
    nodes += matchMid(writing.substring(fr, wbl), reading.substring(fr, rbl))
    if (bck != 0) {
      nodes += StringNode(reading.substring(rbl, reading.length))
    }
    ListNode(nodes.toList)
  }
}
