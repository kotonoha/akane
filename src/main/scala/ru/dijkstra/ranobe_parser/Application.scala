package ru.dijkstra.ranobe_parser

import java.io._
import scalax.file.Path
import java.nio.{BufferUnderflowException, ByteBuffer}
import annotation.tailrec


abstract class Token
case class KanjiToken(text: String) extends Token
case class KanaToken(text: String) extends Token
case class RomajiToken(text: String) extends Token
case class PunctToken(text: String) extends Token

object Application {
  private def buildToken(in: String) : List[Token] = {
    import org.eiennohito.stolen_utils.UnicodeUtil.{isKana, isKanji}
    if (isKanji(in.charAt(0).toInt)) return KanjiToken(in) :: Nil
    if (isKana(in.charAt(0).toInt)) return KanaToken(in) :: Nil
    if (in.charAt(0).isLetter) return RomajiToken(in) :: Nil
    in.toCharArray.toList map {a => PunctToken(a.toString)}
  }

  private def chType(in: Int) : Int = {
    import org.eiennohito.stolen_utils.UnicodeUtil.{isKana, isKanji}
    if (isKana(in)) return 2
    if (isKanji(in)) return 3
    if (in.toChar.isLetter) return 1
    4
  }

  // ML-STYLE
  @tailrec
  private def tokenize_(in: String, tokenAccum: String, listAccum: List[Token]) : List[Token] = {
    if (in.isEmpty) buildToken(tokenAccum) ::: listAccum
    else
      if (tokenAccum.isEmpty) tokenize_(in.drop(1), in.charAt(0).toString, listAccum)
        else
          if (chType(in.charAt(0)) == chType(tokenAccum.last))
            tokenize_(in.drop(1), tokenAccum ++ in.charAt(0).toString, listAccum)
          else
            tokenize_(in, "", buildToken(tokenAccum) ::: listAccum)
  }

  def tokenize(in: String) : List[Token] = tokenize_(in, "", Nil).reverse

  private val test = """青春を、おかしく［＃「おかしく」に傍点］するのはつきものだ！ """

  def main(args: Array[String]): Unit = {
    println(tokenize(test))

  }
}
