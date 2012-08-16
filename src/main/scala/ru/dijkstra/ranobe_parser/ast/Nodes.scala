package ru.dijkstra.ranobe_parser.ast

/**
 * @author eiennohito
 * @since 15.08.12
 */

sealed trait Node

case class StringNode(s: String) extends Node {}
case class ListNode(inner: List[Node]) extends Node
case class RubyNode(ruby: String, inner: Node) extends Node
case class HighlightNode(inner: Node) extends Node

sealed trait HighLvlNode
case object PageBreak extends HighLvlNode
case object EndLine extends HighLvlNode
case class Sentence(s: Node) extends HighLvlNode
case class Image(uri: String) extends HighLvlNode
