/*
 * Copyright 2012 eiennohito
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

package ws.kotonoha.akane.mecab

import org.chasen.mecab.{MeCabConstants, Node, Lattice, Tagger}
import ws.kotonoha.akane.utils.CalculatingIterator

/**
 * @author eiennohito
 * @since 30.10.12 
 */

case class MecabResult(surf: String, pos: String, info: String)

class NodeIterator(var node: Node) extends CalculatingIterator[MecabResult] {

  private def formatResult(node: Node): Option[MecabResult] = {
    val r = MecabResult(node.getSurface, "", node.getFeature)
    Some(r)
  }

  protected def calculate(): Option[MecabResult] = {
    import MeCabConstants._
    if (node == null) return None
    val nv = node.getStat match {
      case MECAB_BOS_NODE | MECAB_EOS_NODE | MECAB_EON_NODE => {
        node = node.getNext
        calculate()
      }
      case _ => {
        val n = formatResult(node)
        node = node.getNext
        n
      }
    }
    nv
  }
}

class MecabParser {

  private val tagger = new Tagger()

  def parse(s: String): List[MecabResult] = {
    resource.makeManagedResource(new Lattice())(_.delete())(Nil) map (lat => {
      lat.set_sentence(s)
      tagger.parse(lat)
      new NodeIterator(lat.bos_node()).toList
    }) either match {
      case Right(l) => l
      case Left(t) => throw t.head
    }
  }

}
