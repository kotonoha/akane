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

package ws.kotonoha.akane.mecab

import org.scalatest.{Matchers, FreeSpec}

/**
 * @author eiennohito
 * @since 15.11.12 
 */

class InfoExtractorTest extends FreeSpec with Matchers {
  "mecab info extractor" - {
    "extracts an info from a noun" ignore {
      val mp = new MecabParser()
      val n = mp.parse("猫")
      n should have length (1)
      val i = InfoExtractor.parseInfo(n.head)
      i.dicForm should equal ("猫")
      i.surface should equal ("猫")
    }
  }
}
