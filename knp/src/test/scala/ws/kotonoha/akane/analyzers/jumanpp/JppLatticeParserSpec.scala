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

package ws.kotonoha.akane.analyzers.jumanpp

import java.io.{BufferedReader, StringReader}

import org.scalatest.{FreeSpec, Matchers}
import ws.kotonoha.akane.resources.Classpath

/**
  * @author eiennohito
  * @since 2016/07/15
  */
class JppLatticeParserSpec extends FreeSpec with Matchers {
  def input(name: String) = {
    val res = Classpath.fileAsString(name)
    new BufferedReader(new StringReader(res))
  }

  "JppLatticeParser" - {
    val parser = new JppLatticeParser
    "parses example1" in {
      val raw = input("jpp/example1.jpp")
      val lattice = parser.parse(raw)
      val node = lattice.nodes.find(_.canonical == "入る/はいる").get
      node should have (
        'nodeId (90),
        'surface ("はいる")
      )
    }
  }
}
