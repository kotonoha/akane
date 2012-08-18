package ws.kotonoha.akane.render

import ws.kotonoha.akane.ast._
import ws.kotonoha.akane.ast.RubyNode
import ws.kotonoha.akane.ast.StringNode
import ws.kotonoha.akane.ast.ListNode
import ws.kotonoha.akane.render.MetaInfo

/**
 * @author eiennohito
 * @since 17.08.12
 */

case class MetaInfo

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
      case StringNode(str) => sb.append(str)
      case RubyNode(_, n1) => renderCore(n1, sb)
      case ListNode(l) => l.foreach(renderCore(_, sb))
      case HighlightNode(n1) => renderCore(n1, sb)
    }
    sb.length - start
  }
}
