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

package ws.kotonoha.akane.tools

import ws.kotonoha.akane.dict.jmdict.JMDictParser
import scalax.file.Path

/**
 * @author eiennohito
 * @since 14.11.12 
 */

object JmdictWritings {
  def main(args: Array[String]) {
    val jmdic = Path.fromString(args(0))
    val out = Path.fromString(args(0) + ".writ")
    for (in <- jmdic.inputStream) {
      val ents = JMDictParser.parse(in)
      val ostrs = ents.map(_.writing).filter(_.length > 1).map(l => l.map(_.value).mkString(";")).filter(_.length > 2)
      out.writeStrings(ostrs.toTraversable, "\n")
    }
  }
}
