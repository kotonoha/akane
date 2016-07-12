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
