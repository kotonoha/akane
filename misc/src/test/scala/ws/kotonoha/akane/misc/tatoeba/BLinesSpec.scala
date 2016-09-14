/*
 * Copyright 2016 eiennohito (Tolmachev Arseny)
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

package ws.kotonoha.akane.misc.tatoeba

import org.scalatest.{FreeSpec, Matchers}

/**
  * @author eiennohito
  * @since 2016/09/12
  */
class BLinesSpec extends FreeSpec with Matchers {
  "BLinesReader" - {
    "reads unit 其の[01]{その}" in {
      val string = "其の[01]{その}"
      val unit = BLinesReader.readUnit(string)
      unit.reading shouldBe None
      unit.headword shouldBe "其の"
      unit.sense shouldBe 1
      unit.surface.get shouldBe "その"
    }

    "reads unit 家(いえ)[01]" in {
      val string = "家(いえ)[01]"
      val unit = BLinesReader.readUnit(string)
      unit.reading shouldBe Some("いえ")
      unit.headword shouldBe "家"
      unit.sense shouldBe 1
      unit.surface shouldBe None
    }

    "reads unit 可也{かなり}" in {
      val string = "可也{かなり}"
      val unit = BLinesReader.readUnit(string)
      unit.reading shouldBe None
      unit.headword shouldBe "可也"
      unit.sense shouldBe 0
      unit.surface shouldBe Some("かなり")
    }

    "reads unit は|1" in {
      val string = "は|1"
      val unit = BLinesReader.readUnit(string)
      unit.reading shouldBe None
      unit.headword shouldBe "は"
      unit.sense shouldBe 0
      unit.surface shouldBe None
    }

    "reads unit は|1[01]" in {
      val string = "は|1[01]"
      val unit = BLinesReader.readUnit(string)
      unit.reading shouldBe None
      unit.headword shouldBe "は"
      unit.sense shouldBe 1
      unit.surface shouldBe None
    }

    "b-line surface can be restored to original sentence" in {
      val bline = "其の[01]{その} 家(いえ)[01] は 可也{かなり} ぼろ屋[01]~ になる[01]{になっている}"
      val original = "その家はかなりぼろ屋になっている"
      val units = BLinesReader.parseUnits(bline, 0, bline.length)
      units.map(_.original).mkString shouldBe original
    }
  }
}
