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

package ws.kotonoha.akane.transform

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers
import ws.kotonoha.akane.parser.{AozoraParser, AozoraStringInput}
import ws.kotonoha.akane.juman.JumanPipeExecutor
import ws.kotonoha.akane.ast.Sentence

/**
 * @author eiennohito
 * @since 17.08.12
 */

class JumanTransformerTest extends FreeSpec with ShouldMatchers{
  "juman transformer" - {
    "parses something" in {
      val pe = JumanPipeExecutor()
      val jt = new JumanTransformer(pe)
      val ts = new AozoraStringInput("中学二年生、誕生日は四月の頭で、つまり現在十四歳――姉の火憐と違い、髪型は気分と時期によってころころ変える。")
      val tfed = new AozoraParser(ts) map {
        case s: Sentence => jt.transformSentence(s)
        case n => n
      }
      //tfed foreach (println(_))
      tfed should not be empty
    }
  }
}
