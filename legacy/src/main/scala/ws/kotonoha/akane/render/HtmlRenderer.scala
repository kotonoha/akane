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

package ws.kotonoha.akane.render

import ws.kotonoha.akane.ast._
import xml.{Node => XNode, Text, NodeSeq}
import ws.kotonoha.akane.ast.Image
import collection.mutable.ListBuffer
import ws.kotonoha.akane.ast.Node

/**
 * @author eiennohito
 * @since 17.08.12
 */

class HtmlRenderer {

  def renderLowLvl(node: Node, bfr: ListBuffer[XNode]): ListBuffer[XNode] = {
    node match {
      case StringNode(s) => bfr += Text(s)
      case ListNode(lst) => lst.foreach(renderLowLvl(_, bfr))
      case RubyNode(rd, in) => {
        bfr += <ruby>{renderLowLvl(in, new ListBuffer[xml.Node])}<rt>{rd}</rt></ruby>
      }
      case HighlightNode(nd) => bfr += <span class="hl">{renderLowLvl(nd, new ListBuffer[xml.Node])}</span>
    }
    bfr
  }

  def render(in: HighLvlNode): NodeSeq = {
    in match {
      case Image(href) => <img src={href}></img>
      case PageBreak => <br/>
      case EndLine => <br/>
      case Sentence(s) => renderLowLvl(s, new ListBuffer[XNode])
    }
  }
}
