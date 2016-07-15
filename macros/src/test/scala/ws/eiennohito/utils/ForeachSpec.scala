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

package ws.eiennohito.utils

import org.scalatest.{FreeSpec, Matchers}

/**
 * @author eiennohito
 * @since 15/08/14
 */
class ForeachSpec extends FreeSpec with Matchers {
  "Foreach" - {
    "works in simple case" in {
      var smt = 0
      Foreach.fori(3, 5) { i => smt += i }
      smt shouldBe 7
    }

    "works even nested" in {
      var smt = 0
      Foreach.fori(1, 3) { i =>
        Foreach.fori(1, 3) { j =>
          smt += i * j
        }
      }

      smt should be (1 + 2 + 2 + 4)
    }
  }
}
