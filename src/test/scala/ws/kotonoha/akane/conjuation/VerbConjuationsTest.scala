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

package ws.kotonoha.akane.conjuation

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author eiennohito
 * @since 13.11.12 
 */

class VerbConjuationsTest extends FreeSpec with ShouldMatchers {
  "conjuator" - {
    "te iru form of ichidan" in {
      val c = Verb.ichidan("食べる")
      c.teForm.iru.render should be (Some("食べている"))
    }
  }
}
