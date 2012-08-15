package ru.dijkstra.ranobe_parser

import akka.actor._

sealed trait Message

case object ProcessFile extends Message

case class ProcessLine(line: String) extends Message

sealed trait Token

case object NewLine extends Token
case object NewSentence extends Token

case class Punctuation(text: Char) extends Token
case class KanjiExtent(text: String) extends Token
case class KanaExtent(text: String) extends Token
case class RomajiExtent(text: String) extends Token
case class HtmlTag(cont: String) extends Token