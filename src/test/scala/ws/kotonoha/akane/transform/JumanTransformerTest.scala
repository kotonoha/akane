package ws.kotonoha.akane.transform

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers
import ws.kotonoha.akane.parser.{AozoraParser, StringInput}
import ws.kotonoha.akane.juman.PipeExecutor
import ws.kotonoha.akane.ast.Sentence

/**
 * @author eiennohito
 * @since 17.08.12
 */

class JumanTransformerTest extends FreeSpec with ShouldMatchers{
  "juman transformer" - {
    "parses something" in {
      val pe = new PipeExecutor("juman.exe")
      val jt = new JumanTransformer(pe)
      val ts = new StringInput("中学二年生、誕生日は四月の頭で、つまり現在十四歳――姉の火憐と違い、髪型は気分と時期によってころころ変える。")
      val tfed = new AozoraParser(ts) map {
        case s: Sentence => jt.transformSentence(s)
        case n => n
      }
      tfed foreach (println(_))
    }
  }
}
