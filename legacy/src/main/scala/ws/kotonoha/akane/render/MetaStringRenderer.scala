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
import ws.kotonoha.akane.ast.RubyNode
import ws.kotonoha.akane.ast.StringNode
import ws.kotonoha.akane.ast.ListNode

/**
  * @author eiennohito
  * @since 17.08.12
  */
case class MetaInfo()

case class MetaString(data: String, info: MetaInfo)

class MetaStringRenderer {
  def render(n: Node) = {
    val sb = new StringBuilder
    renderCore(n, sb)
    MetaString(sb.toString(), new MetaInfo())
  }

  private def renderCore(n: Node, sb: StringBuilder): Int = {
    val start = sb.length
    n match {
      case StringNode(str)   => sb.append(str)
      case RubyNode(_, n1)   => renderCore(n1, sb)
      case ListNode(l)       => l.foreach(renderCore(_, sb))
      case HighlightNode(n1) => renderCore(n1, sb)
    }
    sb.length - start
  }
}
