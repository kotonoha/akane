package ru.dijkstra.ranobe_parser

import akka.actor._
import tokenizer.Token

sealed trait Message

case object ProcessFile extends Message

case class ProcessLine(line: String) extends Message

case class ParseSentence(sentence: String) extends Message
case object Shutdown extends Message
