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

object Tokenizer {
  private def buildToken(in: String) : List[Token] = {
    import org.eiennohito.stolen_utils.UnicodeUtil.{isKana, isKanji}
    if (isKanji(in.charAt(0).toInt)) return KanjiToken(in) :: Nil
    if (isKana(in.charAt(0).toInt)) return KanaToken(in) :: Nil
    if (in.charAt(0).isLetter) return RomajiToken(in) :: Nil
    in.toCharArray.toList map {a => PunctToken(a.toString)} reverse
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

}

abstract class Node
case class TextNode(text: String, bakuten: Boolean) extends Node
case class RubyNode(text: String) extends Node
case object LineBreak extends Node
case object SentenceBreak extends Node
case object PageBreak extends Node
case object ServiceNodeStart extends Node
case object ServiceNodeEnd extends Node
case object QuotationStart extends Node
case object QuotationEnd extends Node
case object BautenMark extends Node
case object PageBreakMark extends Node

object Parser {
  private def buildNode(in: String, buildingRuby: Boolean) : List[Node] =
    if (in.isEmpty) Nil else
      if (buildingRuby) RubyNode(in) :: Nil
  else TextNode(in, false) :: Nil

  // Stage 1 parsing: interpreting tokens
  private def parse_1stStage(in: List[Token], accum: List[Node],
                     buildingRuby : Boolean, buildingServiceNode: Boolean) : List[Node] = {
    if (in.isEmpty) return accum
    if (buildingRuby && buildingServiceNode) throw new Exception("Error: Fuck nested nodes!")
    in.head match {
      case PunctToken("［") =>
      if (in.tail.head == PunctToken("＃")) {
        if (buildingServiceNode) throw new Exception("Error: Nested service nodes")
        return parse_1stStage(in.drop(2), ServiceNodeStart :: accum, buildingRuby, true)
      } else return parse_1stStage(in.drop(1), buildNode("［", buildingRuby) ::: accum, buildingRuby, buildingServiceNode)
      case PunctToken("\n") => return parse_1stStage(in.drop(1), LineBreak :: accum, buildingRuby, buildingServiceNode)
      case PunctToken("］") => if (buildingServiceNode)
        return parse_1stStage(in.drop(1), ServiceNodeEnd :: accum, buildingRuby, false)
        else return parse_1stStage(in.drop(1), buildNode("］", buildingRuby) ::: accum, buildingRuby, buildingServiceNode)
      case PunctToken("《") => {
        if (buildingRuby) throw new Exception("Error: Nested Ruby nodes")
        return parse_1stStage(in.drop(1), accum, true, buildingServiceNode)
      }
      case PunctToken("》") => {
        if (!buildingRuby) throw new Exception("Error: unmatched Rubt node end")
        return parse_1stStage(in.drop(1), accum, false, buildingServiceNode)
      }
      case PunctToken("｜") => return parse_1stStage(in.drop(1), accum, buildingRuby, buildingServiceNode)

      case PunctToken("「") => if (buildingServiceNode) return parse_1stStage(in.drop(1), accum, buildingRuby, buildingServiceNode)
      else return parse_1stStage(in.drop(1), QuotationStart :: accum, buildingRuby, buildingServiceNode)

      case PunctToken("」") => if (buildingServiceNode) return parse_1stStage(in.drop(1), accum, buildingRuby, buildingServiceNode)
      else return parse_1stStage(in.drop(1), QuotationEnd :: accum, buildingRuby, buildingServiceNode)

      case PunctToken(a) => return parse_1stStage(in.drop(1), buildNode(a, buildingRuby) ::: accum, buildingRuby, buildingServiceNode)

      case KanaToken("に") => if (buildingServiceNode && in.tail.head == KanjiToken("傍点"))
        return parse_1stStage(in.drop(2), BautenMark :: accum, buildingRuby, buildingServiceNode)
        else return parse_1stStage(in.drop(1), buildNode("に", buildingRuby) ::: accum, buildingRuby, buildingServiceNode)
      case KanjiToken("改") => if (buildingServiceNode && in.tail.head == KanaToken("ページ"))
        return parse_1stStage(in.drop(2), PageBreakMark :: accum, buildingRuby, buildingServiceNode)
      else return parse_1stStage(in.drop(1), buildNode("改", buildingRuby) ::: accum, buildingRuby, buildingServiceNode)

      case KanaToken(a) => return parse_1stStage(in.drop(1), buildNode(a, buildingRuby) ::: accum, buildingRuby, buildingServiceNode)
      case KanjiToken(a) => return parse_1stStage(in.drop(1), buildNode(a, buildingRuby) ::: accum, buildingRuby, buildingServiceNode)
      case RomajiToken(a) => return parse_1stStage(in.drop(1), buildNode(a, buildingRuby) ::: accum, buildingRuby, buildingServiceNode)
      case _ => throw new Exception("Error: Unknown token")
    }
  }

  // Stage2 parsing: dealing with service nodes
  def parse_2ndStage(in: List[Node], accum: List[Node], bauten_list: List[TextNode], buildingList : Boolean) : List[Node] = {
    if (in.isEmpty) return accum
    in.head match {
      case ServiceNodeEnd =>
        if (in.tail.head == BautenMark) parse_2ndStage(in.tail, accum, bauten_list, true)
        else if (in.tail.head == PageBreakMark) parse_2ndStage(in.tail, PageBreak :: accum, bauten_list, buildingList)
        else throw new Exception("Error: unknown service node")
      case ServiceNodeStart => parse_2ndStage(in.tail, accum, bauten_list, false)
      case tn@TextNode(a, _) => if (buildingList)
        parse_2ndStage(in.tail, accum, TextNode(a, false)::bauten_list, false)
        else if (bauten_list contains tn)
          parse_2ndStage(in.tail, TextNode(a, true) :: accum, bauten_list filterNot({b => b != TextNode(a, false)}), false)
        else parse_2ndStage(in.tail, TextNode(a, false) :: accum, bauten_list, false)
      case BautenMark => parse_2ndStage(in.tail, accum, bauten_list, buildingList)
      case PageBreakMark => parse_2ndStage(in.tail, accum, bauten_list, buildingList)
      case a => parse_2ndStage(in.tail, a :: accum, bauten_list, buildingList)
    }
  }

  def parse(in: List[Token]) : List[Node] = parse_2ndStage(parse_1stStage(in, Nil, false, false),Nil, Nil, false)
}


object Application {
  private val test = "青春を、おかしく［＃「おかしく」に傍点］するのはつきものだ！\n 戦場《せんじょう》ヶ｜原《はら》ひたぎは\n［「おかしく」に傍点］［＃改ページ］改ページ"
  private val test2 = "阿良々木《あららぎ》暦《こよみ》を目がけて空から降ってきた女の子・戦場《せんじょう》ヶ｜原《はら》ひたぎには、およそ体重と呼べるようなものが、全くと言っていいほど、なかった――!?"
  def main(args: Array[String]): Unit = {
    println("----")
    println(Parser.parse(Tokenizer.tokenize(test2)))
  }
}
