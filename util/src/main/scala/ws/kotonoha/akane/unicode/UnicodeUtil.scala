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

package ws.kotonoha.akane.unicode

import java.io.{Reader, StringReader}

/**
  * @author eiennohito
  * @since 02.03.12
  */
object UnicodeUtil {
  val kanjiRanges = Array(0x4e00 -> 0x9fff,
                          0x3400 -> 0x4dbf,
                          0x20000 -> 0x2a6df,
                          0x2a700 -> 0x2b73f,
                          0x2b840 -> 0x2b81f)

  val hiraganaRange = Array(0x3040 -> 0x309f, 0x1b000 -> 0x1b0ff)

  val katakanaRange = Array(0x30a0 -> 0x30ff, 0xff60 -> 0xff96, 0x32d0 -> 0x32fe, 0x31f0 -> 0x31ff)

  def inRange(c: Int, ranges: Array[(Int, Int)]): Boolean = {
    var i = 0
    val rend = ranges.length
    while (i < rend) {
      val rng = ranges(i)
      val begin = rng._1
      val end = rng._2
      if (begin <= c && c <= end) return true
      i += 1
    }

    false
  }

  def isKanji(c: Int) = inRange(c, kanjiRanges)

  def isHiragana(p: Int): Boolean = inRange(p, hiraganaRange)

  def isHiragana(s: String): Boolean = stream(s).forall(isHiragana)

  def isKatakana(p: Int): Boolean = inRange(p, katakanaRange)

  def isKatakana(s: String): Boolean = stream(s).forall(isKatakana)

  def isKana(p: Int): Boolean = isHiragana(p) || isKatakana(p)

  def isKana(s: String): Boolean = stream(s).forall(isKana)

  def hasKanji(s: String): Boolean = stream(s).exists(isKanji)
  def hasKanji(s: String, start: Int, end: Int): Boolean = stream(s, start, end).exists(isKanji)

  def hasKana(s: String) = stream(s).exists(isKana)

  def isJapanese(c: Int): Boolean = isKanji(c) || isHiragana(c) || isKatakana(c)

  def isJapanese(s: String): Boolean = stream(s).take(50).forall { isJapanese }

  def kanji(s: String) =
    stream(s).filter(isKanji).map(cp => new String(Character.toChars(cp))).toList

  def stream(s: CharSequence): SeqCodepointIterator = stream(s, 0, s.length)
  def stream(s: CharSequence, start: Int, end: Int): SeqCodepointIterator =
    new SeqCodepointIterator(s, start, end)

  def stream(r: Reader): CodepointIterator = new ReaderCodepointIterator(r)

  def klen(s: String): Int = stream(s).count(isKanji)

}

trait CodepointIterator extends Iterator[Int]

final class ReaderCodepointIterator(input: Reader) extends CodepointIterator {

  private[this] def computeNext(): Int = {
    val c1 = input.read()
    if (c1 == -1) return -1
    if (Character.isLowSurrogate(c1.toChar)) {
      val c2 = input.read()
      if (c2 == -1) return -1
      Character.toCodePoint(c1.toChar, c2.toChar)
    } else c1
  }

  private[this] var nextCp = computeNext()
  override def hasNext: Boolean = nextCp == -1
  override def next(): Int = {
    val c = nextCp
    nextCp = computeNext()
    c
  }
}

final class SeqCodepointIterator(input: CharSequence, from: Int, to: Int)
    extends CodepointIterator {
  private[this] var position = init()

  private def init() = {
    if (from >= input.length()) {
      to
    } else if (Character.isLowSurrogate(input.charAt(from))) {
      from + 1
    } else {
      from
    }
  }

  def reset(): SeqCodepointIterator = {
    position = init()
    this
  }

  override def hasNext: Boolean = {
    position < to
  }

  override def next(): Int = {
    val cp = Character.codePointAt(input, position)
    position += Character.charCount(cp)
    cp
  }
}
