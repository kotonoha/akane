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

/**
 * @author eiennohito
 * @since 15/07/08
 */
class KnpTableTest extends FreeSpec with Matchers {
  "KnpTable" - {
    "calculates correct scope" in {
      val tree = TreeUtil.classpath("trees/weirdKihonku.txt")
      val res = tree.bunsetsuScope(Array(0, 1))
      res shouldEqual Array(0)
    }

    "calculates bunsetsu index for kihonku" in {
      val tree = TreeUtil.classpath("trees/bunsetsu-1.txt")
      tree.bunsetsuIdxForKihonku(6) shouldBe 5
    }
  }
}
