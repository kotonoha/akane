package ru.dijkstra.ranobe_parser
import akka.actor._
import annotation.tailrec

class TokenizerActor(pipeOutput: Actor) extends Actor{

  protected def buildToken(in: StringBuilder) : List[Token] = {
    import org.eiennohito.stolen_utils.UnicodeUtil.{isKana, isKanji}
    if (in.isEmpty) return Nil
    if (isKanji(in.head.toInt)) return KanjiExtent(in.toString()) :: Nil
    if (isKana(in.head.toInt)) return KanaExtent(in.toString()) :: Nil
    if (in.head.isLetter) return RomajiExtent(in.toString()) :: Nil
    if (in.head == '<') {
      val end = in indexWhere {a => a == '>'}
      if (end == -1) return Punctuation('<') :: buildToken(in tail)
      val tags = in splitAt end
      return HtmlTag(tags._1.toString()) :: buildToken(tags._2)
    }
    (in.toString().toCharArray.toList map {
      a => Punctuation(a)
    }).reverse
  }
  protected def chType(in: Int) : Int = {
    import org.eiennohito.stolen_utils.UnicodeUtil.{isKana, isKanji}
    if (isKana(in)) return 2
    if (isKanji(in)) return 3
    if (in.toChar.isLetter) return 1
    4
  }

  @tailrec
  private[ranobe_parser] def tokenize_(in: String, tokenAccum: StringBuilder, listAccum: List[Token]) : List[Token] = {
    if (in.isEmpty) buildToken(tokenAccum) ::: listAccum
    else
    if (tokenAccum.isEmpty) tokenize_(in.tail, new StringBuilder(in.head), listAccum)
    else
    if (chType(in.head) == chType(tokenAccum.last))
      tokenize_(in.tail, tokenAccum.append(in.head), listAccum)
    else
      tokenize_(in, new StringBuilder(), buildToken(tokenAccum) ::: listAccum)
  }

  protected def receive = {
    case ProcessLine(line) => {

    }
  }
}
