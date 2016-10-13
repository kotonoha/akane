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

import org.apache.commons.lang3.StringUtils
import ws.kotonoha.akane.kytea.wire.{KyteaSentence, KyteaUnit}

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

/**
  * @author eiennohito
  * @since 2016/09/06
  */
class KyteaFormat(cfg: KyteaConfig) {

  @tailrec
  private def parseMorphmemeImpl(buffer: ArrayBuffer[String], line: CharSequence, start: Int): Unit = {
    val endOfLexeme = StringUtils.indexOf(line, cfg.tagBound, start)
    if (endOfLexeme == -1) {
      buffer += line.subSequence(start, line.length()).toString
    } else {
      buffer += line.subSequence(start, endOfLexeme).toString
      parseMorphmemeImpl(buffer, line, endOfLexeme + 1)
    }
  }


  def parseMorpheme(sequence: CharSequence): KyteaUnit = {
    val parts = new ArrayBuffer[String](4)
    parseMorphmemeImpl(parts, sequence, 0)
    KyteaUnit(parts)
  }

  @tailrec
  private def parseSentenceImpl(buffer: ArrayBuffer[KyteaUnit], line: CharSequence, start: Int): Unit = {
    val endOfLexeme = StringUtils.indexOf(line, cfg.wordBound, start)
    if (endOfLexeme == -1) {
      buffer += parseMorpheme(line.subSequence(start, line.length()))
    } else {
      buffer += parseMorpheme(line.subSequence(start, endOfLexeme))
      parseSentenceImpl(buffer, line, endOfLexeme + 1)
    }
  }

  def parse(line: CharSequence): KyteaSentence = {
    val buffer = new ArrayBuffer[KyteaUnit](32)
    parseSentenceImpl(buffer, line, 0)
    KyteaSentence(buffer)
  }
}
