package ws.kotonoha.akane.transform

import ws.kotonoha.akane.ast.{ListNode, RubyNode, StringNode, Sentence}
import ws.kotonoha.akane.juman.PipeExecutor
import ws.kotonoha.akane.render.MetaStringRenderer
import ws.kotonoha.akane.JumanRW
import ws.kotonoha.akane.ruby.AfterLexical

/**
 * @author eiennohito
 * @since 17.08.12
 */

class JumanTransformer(j: PipeExecutor) {
  def transformSentence(s: Sentence): Sentence = {
    val info = new MetaStringRenderer().render(s.s)
    val parsed = j.parse(info.data)
    val tfed = parsed map {
      case JumanRW(w, r) => AfterLexical.makeNode(w, r)
    }
    Sentence(ListNode(tfed))
  }
}
