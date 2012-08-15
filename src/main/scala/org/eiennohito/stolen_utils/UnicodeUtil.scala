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

  def isJapLetter(c: Int) = c == '１' || c == '２' || c == '４' || c == '５' || c == '６' || c == '７' || c == '８' || c == '９'  || c == '０'

  // Digits used ro replace kanji with numerals, so it is kanji in terms of parsing
  def isKanji(c: Int) = inRange(c, kanjiRanges) || c == '々' || isJapLetter(c)

  def isHiragana(p: Int) = inRange(p, hiraganaRange)

  def isKatakana(p: Int) = inRange(p, katakanaRange)

  def isKana(p: Int) = isHiragana(p) || isKatakana(p)
}