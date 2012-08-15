package ru.dijkstra.ranobe_parser

import annotation.tailrec

object Tokenizer {

  private[ranobe_parser] def buildToken(in: StringBuilder) : List[Token] = {
    import org.eiennohito.stolen_utils.UnicodeUtil.{isKana, isKanji}
    if (in.isEmpty) return Nil
    if (in.forall({isKanji(_)})) return KanjiExtent(in.toString()) :: Nil
    if (in.forall({isKana(_)})) return KanaExtent(in.toString()) :: Nil
    //todo: Recognize romaji
    //if (in.forall({_.isLetter})) return RomajiExtent(in.toString()) :: Nil
    val SENTENCE_SEPARATORS = List('。','？', '！', '.', '?', '!', '」', '…')
    in.head match {
      case '<' => {
        val end = in indexWhere { _ == '>'}
        if (end == -1) return Punctuation('<') :: buildToken(in tail)
        val tags = in splitAt end
        HtmlTag(tags._1.append('>').toString()) :: buildToken(tags._2.tail)
      }
      case '《' => RubyNodeStart :: buildToken(in tail)
      case '》' => RubyNodeEnd :: buildToken(in tail)
      case '［' if in.tail.head == '＃' => ServiceNodeStart :: buildToken(in tail)
      case a if SENTENCE_SEPARATORS contains a => Punctuation(a) :: NewSentence  :: buildToken(in tail)
      case a => Punctuation(a) :: buildToken(in tail)
    }
  }
  private def chType(in: Int) : Int = {
    import org.eiennohito.stolen_utils.UnicodeUtil.{isKana, isKanji}
    if (isKana(in)) return 2
    if (isKanji(in)) return 3
    //todo: recognize romaji
    //if (in.toChar.isLetter) return 1
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
}