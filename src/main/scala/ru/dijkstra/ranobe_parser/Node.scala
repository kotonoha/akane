package ru.dijkstra.ranobe_parser

sealed trait Node


case class TextNode (text: String) extends Node
case class RomajiNode (text: String) extends Node
case class KanaNode (text: String) extends Node
case class KanjiNode (text: String) extends Node

case class RubyNode (text: String) extends Node
case class HtmlTagNode (text: String) extends Node

case object PageBreak extends Node
case object LineBreak extends Node
case object SentenceBreakDot extends Node
case object SentenceBreakExclamation extends Node
case object SentenceBreakQuestion extends Node

case object QuotationStart extends Node
case object QuotationEnd extends Node

case object DoubleQuotationStart extends Node
case object DoubleQuotationEnd extends Node

case object Comma extends Node

case object ServiceNodeStart extends Node
case object ServiceNodeEnd extends Node

case object BautenMark extends Node

case class UnidentifiedPunctiation (test: String) extends Node

