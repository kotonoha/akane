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

package ws.kotonoha.akane.dic.jmdict

import java.io.StringReader

import org.scalatest.{FreeSpec, LoneElement, Matchers}
import ws.kotonoha.akane.resources.Classpath
import ws.kotonoha.akane.xml.{XmlParseTransformer, XmlParser}

import scala.collection.mutable.ArrayBuffer

/**
  * @author eiennohito
  * @since 2016/07/21
  */
class JmdictParserSpec extends FreeSpec with Matchers with LoneElement {
  def xml(pth: String) = {
    val content = Classpath.fileAsString(pth)
    val it = XmlParser.create(new StringReader(content))
    new XmlParseTransformer(it, debug = true)
  }

  "JmdictParser" - {
    val parser = new JmdictParser
    "kanji part works" in {
      val data = xml("jmdict/parts_kanji.xml")
      val parsed = parser.parseKanji(data)
      parsed.freq shouldBe None
      parsed.priority.loneElement shouldBe Priority.ichi1
      parsed.content shouldBe "否々"
    }

    "kanji part with prio works" in {
      val data = xml("jmdict/parts_kanji_pri.xml")
      val parsed = parser.parseKanji(data)
      parsed.freq shouldBe Some(10)
      parsed.priority should contain (Priority.news1)
      parsed.content shouldBe "明白"
    }

    "reading part works" in {
      val data = xml("jmdict/parts_reading.xml")
      val parsed = parser.parseReading(data, ArrayBuffer.empty)
      parsed.freq shouldBe None
      parsed.priority.loneElement shouldBe Priority.ichi1
      parsed.content shouldBe "いやいや"
    }

    "enitity works" in {
      val data = xml("jmdict/iyaiya.xml")
      val parsed = data.trans("entry")(parser.parseEntry)
      parsed.writings should have length 2
    }

    "restrictions in sense work" in {
      val data = xml("jmdict/restr_sense.xml")
      val parsed = data.trans("entry")(parser.parseEntry)
      parsed.writings should have length 2
      parsed.meanings should have length 3
      parsed.meanings(1).readingRestriction shouldBe Seq(0, 1)
    }

    "restrictions in reading work" in {
      val data = xml("jmdict/restr_reading.xml")
      val parsed = data.trans("entry")(parser.parseEntry)
      parsed.writings should have length 3
      parsed.meanings should have length 1
      parsed.readings.head.restr shouldBe Seq(0, 1)
    }
  }
}
