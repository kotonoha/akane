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
    val taberu = Verb.ichidan("食べる")
    val kaeru = Verb.godan("帰る")
    val tatakau = Verb.godan("戦う")
    "te iru form of ichidan" in {
      taberu.teForm.iru.render should be (Some("食べている"))
    }

    "te iru form of godan" in {
      kaeru.teForm.iru.render should be (Some("帰っている"))
    }

    "nai form is good" in {
      def nai(in: Verb) = in.naiStem.nai.render.get
      nai(tatakau) should equal("戦わない")
      nai(taberu) should equal("食べない")
      nai(kaeru) should equal("帰らない")
    }

    "masu form works lol" in {
      kaeru.masuStem.masu.taStem.ta.render should be (Some("帰りました"))
    }

    "masu with masu" in {
      val verb = Verb.godan("増す")
      verb.masuStem.render.get should equal("増し")
    }
  }

  "chaining in conjuator" - {
    "works" in {
      val v = Verb.dummy
      val verb = Verb.ichidan("食べる")
      val rules = v.generate(5)
      println("All chains (5) from verb")
      /*rules.map(rule => {
        val form = rule.tf(verb).render.get
        val name = rule.chain.mkString("->")
        "%s\t\t%s".format(form, name)
      }).foreach(println(_))*/
      rules should not have length (0)
    }
  }
}
