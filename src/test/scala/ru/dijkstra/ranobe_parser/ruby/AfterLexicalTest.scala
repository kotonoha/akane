package ru.dijkstra.ranobe_parser.ruby

import ru.dijkstra.ranobe_parser.ast.{RubyNode, ListNode, StringNode}

class AfterLexicalTest extends org.scalatest.FunSuite with org.scalatest.matchers.ShouldMatchers {
  test("makes kana node from kana soruce") {
    AfterLexical.makeNode("ばか", "ばか") should equal (StringNode("ばか"))
  }

  test("trims end from mixed source") {
    val node = AfterLexical.makeNode("眠り", "ねむり")
    val lst = ListNode( List(RubyNode("ねむ", StringNode("眠")), StringNode("り")))
    node should equal (lst)
  }
}
