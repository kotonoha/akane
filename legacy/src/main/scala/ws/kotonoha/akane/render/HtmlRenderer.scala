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
