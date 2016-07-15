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

import ws.kotonoha.akane.ast.{ListNode, Sentence}
import ws.kotonoha.akane.juman.JumanPipeExecutor
import ws.kotonoha.akane.pipe.juman.JumanRW
import ws.kotonoha.akane.render.MetaStringRenderer
import ws.kotonoha.akane.ruby.AfterLexical

/**
 * @author eiennohito
 * @since 17.08.12
 */

class JumanTransformer(j: JumanPipeExecutor) {
  def transformSentence(s: Sentence): Sentence = {
    val info = new MetaStringRenderer().render(s.s)
    val parsed = j.parse(info.data)
    val tfed = parsed map {
      case JumanRW(w, r) => AfterLexical.makeNode(w, r)
    }
    Sentence(ListNode(tfed))
  }
}
