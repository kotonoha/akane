package ws.kotonoha.akane.analyzers.kytea

import java.util.regex.Pattern

import org.apache.commons.lang3.StringUtils

import scala.collection.generic.Growable

case class RawMorpheme(fields: IndexedSeq[String])

/**
  * @author eiennohito
  * @since 2015/12/09
  */
class KyteaReader(morphSep: Char, fieldSep: Char, fields: Int) {

  private val fieldRegex = Pattern.compile(Pattern.quote(fieldSep.toString))
  private val morphRegex = Pattern.compile(Pattern.quote(morphSep.toString))


  def processMorph(input: CharSequence, start: Int, end: Int, sink: Growable[RawMorpheme]): Unit = {
    val sseq = input.subSequence(start, end)
    val seq = fieldRegex.split(sseq)
    sink += RawMorpheme(seq)
  }

  def readTo(input: CharSequence, sink: Growable[RawMorpheme]): Int = {
    var pos = 0
    var end = StringUtils.indexOf(input, morphSep, pos)

    var procesed = 0

    while (end != -1) {
      processMorph(input, pos, end, sink)
      pos = end + 1
      end = StringUtils.indexOf(input, morphSep, pos)
      procesed += 1
    }

    processMorph(input, pos, input.length(), sink)

    procesed
  }
}
