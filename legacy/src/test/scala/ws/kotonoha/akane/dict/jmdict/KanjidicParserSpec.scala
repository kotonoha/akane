/*
 * Copyright 2012 eiennohito
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

package ws.kotonoha.akane.dict.jmdict

import org.scalatest.{FreeSpec, Matchers}
import ws.kotonoha.akane.dict.kanjidic2.Kanjidic2Parser

/**
 * @author eiennohito
 * @since 17.01.13 
 */

class KanjidicParserSpec extends FreeSpec with Matchers {

  "kradfile parser" - {
    "parses something" in {
      val inp = getClass.getClassLoader.getResourceAsStream("kanjidic_frag_01.xml")
      val kanji = Kanjidic2Parser.parse(inp)
      val lst = kanji.toList
      lst should have length (1)
      val item = lst.head
      item.literal should be ("唖")
    }
  }

}
