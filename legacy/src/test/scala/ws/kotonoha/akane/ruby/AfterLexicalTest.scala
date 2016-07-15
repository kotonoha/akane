/*
 * Copyright 2012-2016 eiennohito (Tolmachev Arseny)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ws.kotonoha.akane.ruby

import ws.kotonoha.akane.ast.{RubyNode, ListNode, StringNode}

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
