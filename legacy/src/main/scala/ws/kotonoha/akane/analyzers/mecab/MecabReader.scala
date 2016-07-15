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

package ws.kotonoha.akane.analyzers.mecab

import java.util.regex.Pattern

import ws.kotonoha.akane.utils.DelimetedIterator


case class RawMecabSentence(morphemes: Seq[RawMecabMorpheme])
case class RawMecabMorpheme(parts: IndexedSeq[String])

/**
  * @author eiennohito
  * @since 2015/12/09
  */
class MecabReader {
  private val columnRegex = Pattern.compile("\t")
  def readOne(inp: DelimetedIterator) = {
    val lines = inp.map{ line =>
      val fields = columnRegex.split(line)
      RawMecabMorpheme(fields)
    }
    RawMecabSentence(lines.toIndexedSeq)
  }
}
