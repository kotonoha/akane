package ru.dijkstra.ranobe_parser.ast

/**
 * @author eiennohito
 * @since 15.08.12
 */

sealed trait Node

case class StringNode(s: String) extends Node {}
case class ListNode(inner: List[Node]) extends Node
case class RubyNode(ruby: String, inner: Node) extends Node
