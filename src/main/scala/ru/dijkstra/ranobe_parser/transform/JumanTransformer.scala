package ru.dijkstra.ranobe_parser.transform

import ru.dijkstra.ranobe_parser.ast.{ListNode, RubyNode, StringNode, Sentence}
import ru.dijkstra.ranobe_parser.juman.PipeExecutor
import ru.dijkstra.ranobe_parser.render.MetaStringRenderer
import ru.dijkstra.ranobe_parser.JumanRW
import ru.dijkstra.ranobe_parser.ruby.AfterLexical

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
