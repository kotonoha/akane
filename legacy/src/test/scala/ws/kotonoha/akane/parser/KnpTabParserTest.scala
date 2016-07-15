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

package ws.kotonoha.akane.parser

import org.scalatest.{Matchers, FreeSpec}
import org.scalatest.matchers.ShouldMatchers
import scalax.io.{Codec, Resource}

/**
 * @author eiennohito
 * @since 2014-05-08
 */
class KnpTabParserTest extends FreeSpec with Matchers {
  "KnpTabParser" - {
    val parser = new KnpTabFormatParser
    "parses a small tree" in {
      val lines = Resource.fromClasspath("knp.tab.txt").lines()
      val result = parser.parse(lines)
      result.bunsetsuCnt shouldBe 5
      result.bunsetsu(4).lexemes.head.reading should be("み")
    }
  }
}


object TreeUtil {
  def classpath(name: String) = {
    val parser = new KnpTabFormatParser
    val lines = Resource.fromClasspath(name).lines()(Codec.UTF8)
    val res = parser.parse(lines)
    res
  }
}
