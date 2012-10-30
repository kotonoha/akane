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

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author eiennohito
 * @since 30.10.12 
 */

class ParserWorks extends FreeSpec with ShouldMatchers {
  "mecab parser" - {
    MecabInit.init()
    "works" in {
      val str = "私は猫であるしぼまない"
      val parser = new MecabParser
      val lst = parser.parse(str)
      lst should not be (Nil)
      lst.map(_.surf).mkString should equal (str)
    }
  }

  "jdic info" - {
    "original writing is being restored" in {
      val dform = "すりきる"
      val dread = "スリキラ"
      val vars = "すりきら/摺り切ら/摺切ら/擦り切ら/擦切ら"
      InfoExtractor.restoreWriting(dform, dread, vars) should equal("摺り切る")
    }

    "original reading is being restored" in {
      //動詞,自立,*,*,一段,未然形,くぐり抜ける,クグリヌケ,クグリヌケ,くぐりぬけ/くぐり抜/くぐり抜け/潜り抜/潜り抜け/潜抜/潜抜け
      val dform = "くぐり抜ける"
      val info = "くぐりぬけ/くぐり抜/くぐり抜け/潜り抜/潜り抜け/潜抜/潜抜け"
      InfoExtractor.restoreReading(dform, info) should equal ("くぐりぬける")
    }

    "original reading is being restored when form differs" in {
      val dform = "くぐり抜ける"
      val info = "くぐりぬけよ/くぐり抜けよ/くぐり抜よ/潜り抜けよ/潜り抜よ/潜抜けよ/潜抜よ"
      InfoExtractor.restoreReading(dform, info) should equal ("くぐりぬける")
    }

    "original reading is being restored when form differs2" in {
      val dform = "くぐり抜ける"
      val info = "くぐりぬけりゃ/くぐり抜けりゃ/くぐり抜りゃ/潜り抜けりゃ/潜り抜りゃ/潜抜けりゃ/潜抜りゃ"
      InfoExtractor.restoreReading(dform, info) should equal ("くぐりぬける")
    }

    "original reading is being restored when form differs3 godan" in {
      val dform = "交ぜ返す"
      val info = "まぜかえせ/まぜ返せ/交ぜかえせ/交ぜ返せ/混ぜかえせ/混ぜ返せ/雑ぜ返せ/雑返せ"
      InfoExtractor.restoreReading(dform, info) should equal ("まぜかえす")
    }
  }
}
