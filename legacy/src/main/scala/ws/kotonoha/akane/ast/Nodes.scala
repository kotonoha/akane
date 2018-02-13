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

package ws.kotonoha.akane.ast

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
