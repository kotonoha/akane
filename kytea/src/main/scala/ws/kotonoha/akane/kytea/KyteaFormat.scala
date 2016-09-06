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

package ws.kotonoha.akane.kytea

import java.util.regex.Pattern

import org.apache.commons.lang3.StringUtils

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

/**
  * @author eiennohito
  * @since 2016/09/06
  */
object KyteaFormat {

  private val morphemeSplit = Pattern.compile(Pattern.quote(KyteaConfig.tagBound))

  private def parseMorpheme(sequence: CharSequence): KyteaMorpheme = {
    val splitted = morphemeSplit.split(sequence)
    KyteaMorpheme(splitted)
  }

  @tailrec
  private def parseImpl(buffer: ArrayBuffer[KyteaMorpheme], line: CharSequence, start: Int): Unit = {
    val endOfLexeme = StringUtils.indexOf(line, KyteaConfig.wordBound, start)
    if (endOfLexeme == -1) {
      buffer += parseMorpheme(line.subSequence(start, line.length()))
    } else {
      buffer += parseMorpheme(line.subSequence(start, endOfLexeme))
      parseImpl(buffer, line, endOfLexeme)
    }
  }

  def parse(line: CharSequence): Seq[KyteaMorpheme] = {
    val buffer = new ArrayBuffer[KyteaMorpheme](32)
    parseImpl(buffer, line, 0)
    buffer
  }
}
