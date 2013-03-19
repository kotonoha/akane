/*
 * Copyright 2012-2013 eiennohito
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

package ws.kotonoha.akane.unicode

/**
 * @author eiennohito
 * @since 19.03.13 
 */
/**
 * Miscellaneous text utilities
 */
object KanaUtil {
  /**
   * Converts all Katakana characters to hiragana characters
   */
  def kataToHira(input: String): String = {
    val buf: StringBuffer = new StringBuffer(input)
    val length: Int = buf.length
    var i: Int = 0
    while (i < length) {
      val ch: Char = buf.charAt(i)
      if (ch >= 0x30a1 && ch < 0x30f4) {
        buf.setCharAt(i, (ch - 96).toChar)
      }
      i += 1
    }
    buf.toString
  }
}



