package org.eiennohito.stolen_utils

/**
 * @author eiennohito
 * @since 02.03.12
 */
object UnicodeUtil {
  val kanjiRanges = Array(0x4e00 -> 0x9fff,
    0x3400 -> 0x4dbf, 0x20000 -> 0x2a6df,
    0x2a700 -> 0x2b73f, 0x2b840 -> 0x2b81f)
  val hiraganaRange = Array(0x3040 -> 0x309f,
    0x1b000 -> 0x1b0ff)
  val katakanaRange = Array(0x30a0 -> 0x30ff,
    0xff60 -> 0xff96, 0x32d0 -> 0x32fe,
    0x31f0 -> 0x31ff)

  def inRange(c: Int, ranges: Array[(Int, Int)]): Boolean = {
    ranges.foldLeft(false) {
      case (p, (begin, end)) => if (p) p else begin <= c && c <= end
    }
  }

  def isKanji(c: Int) = inRange(c, kanjiRanges)

  def isHiragana(p: Int) = inRange(p, hiraganaRange)

  def isKatakana(p: Int) = inRange(p, katakanaRange)

  def isKana(p: Int) = isHiragana(p) || isKatakana(p)
}