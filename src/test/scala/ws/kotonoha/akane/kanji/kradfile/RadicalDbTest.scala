/*
 * Copyright 2012-2013 eiennohito
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

package ws.kotonoha.akane.kanji.kradfile

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FreeSpec

/**
 * @author eiennohito
 * @since 08.07.13 
 */

class RadicalDbTest extends FreeSpec with ShouldMatchers {
  "RadicalDb" - {
    "finds some kanji" in {
      val list = RadicalDb.table.get("私")
      list should (have size 2)
    }

    "finds reverse kanji" in {
      val reverse = RadicalDb.reverse.get("厶")
      reverse should not have size(0)
    }

    "finds similar kanji" in {
      val similar = SimilarKanji.find("私")
      similar should not have size(0)
    }
  }
}
