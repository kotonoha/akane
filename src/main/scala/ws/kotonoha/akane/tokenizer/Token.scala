package ws.kotonoha.akane.tokenizer

sealed trait Token

case object NewLine extends Token
case object NewSentence extends Token
case object ServiceNodeStart extends Token
case object ServiceNodeEnd extends Token
case object RubyNodeStart extends Token
case object RubyNodeEnd extends Token

case class Punctuation(text: Char) extends Token
case class KanjiExtent(text: String) extends Token
case class KanaExtent(text: String) extends Token
case class RomajiExtent(text: String) extends Token
case class HtmlTag(cont: String) extends Token
