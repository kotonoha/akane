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

package ws.kotonoha.akane.parser

import java.nio.CharBuffer

/**
  * @author eiennohito
  * @since 2014-10-28
  */
class CharBufferLineIterator(buffer: CharBuffer, globalSeparator: CharSequence)
    extends Iterator[CharSequence] {
  var processing = true

  import ws.kotonoha.akane.parser.CharBufferLineIterator._

  def positionOf(char: Char): Int = {
    var pos = buffer.position()
    val lim = buffer.limit()
    while (pos < lim) {
      if (char == buffer.get(pos)) {
        return pos
      }
      pos += 1
    }
    -1
  }

  def checkEnd() = {
    equalsTo(buffer, globalSeparator) && buffer.remaining() > 0
  }

  def skipSeparator() = {
    if (equalsTo(buffer, globalSeparator)) {
      buffer.position(buffer.position() + globalSeparator.length())
    }
  }

  override def hasNext = processing

  override def next() = {
    val endl = positionOf('\n')
    if (endl == -1) {
      processing = false
      buffer
    } else {
      val buf = buffer.subSequence(0, buffer.position() - endl - 1)
      buffer.position(endl + 1)
      processing = equalsTo(buf, globalSeparator)
      buf
    }
  }
}

object CharBufferLineIterator {

  val eol = "EOL\n"

  def equalsTo(buf: CharBuffer, cseq: CharSequence): Boolean = {
    val pos = buf.position()
    var i = 0
    val end = Math.min(cseq.length(), buf.remaining())
    while (i < end) {
      if (buf.get(pos + i) != cseq.charAt(i)) {
        return false
      }
      i += 1
    }
    true
  }

}
