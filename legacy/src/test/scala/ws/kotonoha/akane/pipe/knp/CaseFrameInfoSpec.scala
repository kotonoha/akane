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

package ws.kotonoha.akane.pipe.knp

import org.scalatest.{Matchers, FreeSpec}
import ws.kotonoha.akane.parser.KnpTabFormatParser

import scalax.io.Resource

/**
 * @author eiennohito
 * @since 15/02/20
 */
class CaseFrameInfoSpec extends FreeSpec with Matchers {
  "case frame info" - {
    "parse usages" - {
      "regular one is parsed without problems" in {
        val usage = "修飾/C/何も/4/0/3"
        val res = CaseFrameInfo.parseUsages(usage)
        res should have length (1)
        val item = res.head
        item should have ('kaku ("修飾"), 'writing ("何も"), 'kihonku (4))
      }

      "unapplied ones should be ignored" in {
        val usage = "ガ/O/連/3/0/2;ヲ/N/支持/8/0/2;カラ/U/-/-/-/-;デ/C/選/6/0/2;ニツク/U/-/-/-/-;時間/U/-/-/-/-;外の関係/U/-/-/-/-"
        val res = CaseFrameInfo.parseUsages(usage)
        res should have length(3)
      }
    }

    "inKihonku" - {
      val parser = new KnpTabFormatParser
      "should parse a test variant" in {
        val treeData = Resource.fromClasspath("trees/bunsetsu-1.txt").lines()
        val tree = parser.parse(treeData)
        val res = CaseFrameInfo.inKihonku(tree.kihonku(7))
        res should be ('defined)
        val item = res.get
        item should have (
          'wordRepr ("得る/える+たい/たい"),
          'kind ("動19")
        )
      }
    }
  }
}
