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

package ws.kotonoha.akane.analyzers.kytea

import org.scalatest.{Matchers, FreeSpec}

import scala.collection.mutable.ArrayBuffer

/**
  * @author eiennohito
  * @since 2015/12/09
  */
class KyteaReaderSpec extends FreeSpec with Matchers {
  "KyteaReaderSpec" - {
    "parses small fragment" in {
      val data = "一/名詞/いち 度/名詞/ど 寝言/名詞/ねごと を/助詞/を 言/動詞/い う/語尾/う"
      val reader = new KyteaReader(' ', '/', 3)
      val buf = new ArrayBuffer[RawMorpheme]()
      reader.readTo(data, buf)
      buf should have length 6
    }
  }
}
