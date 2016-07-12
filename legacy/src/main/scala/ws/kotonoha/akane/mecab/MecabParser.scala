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

import ws.kotonoha.akane.utils.CalculatingIterator
import org.bridj.Pointer
import mecab.{MecabLibrary, mecab_node_t}

/**
 * @author eiennohito
 * @since 30.10.12 
 */

case class MecabResult(surf: String, pos: Int, info: String)

class NodeIterator(var node: Pointer[mecab_node_t]) extends CalculatingIterator[MecabResult] {
  import  ws.kotonoha.akane.bridj.PointerUtil._

  private def formatResult(node: mecab_node_t): Option[MecabResult] = {
    val bts = node.surface()
    val len = node.length()
    val strbts = bts.getBytes(len)
    val s = new String(strbts, "UTF-8")
    val r = MecabResult(s, node.posid(), node.feature().u8s)
    Some(r)
  }

  protected def calculate(): Option[MecabResult] = {
    import mecab.MecabLibrary._
    if (node == null) return None
    val nv = node.get().stat().toInt match {
      case MECAB_BOS_NODE | MECAB_EOS_NODE | MECAB_EON_NODE => {
        node = node.get().next()
        calculate()
      }
      case _ => {
        val n = formatResult(node.get())
        node = node.get().next()
        n
      }
    }
    nv
  }
}

class MecabParser {
  import java.lang.{Byte => JByte}

  private val tagger = {
    val p = Pointer.allocateByte()
    p.set(0.toByte)
    val t = MecabLibrary.mecab_new2(p)
    p.release()
    t
  }

  private var bsize = 4 * 1024L
  private var buffer: Pointer[JByte] = Pointer.allocateBytes(bsize)

  def resize(sz: Long) = {
    buffer.release()
    bsize = sz
    buffer = Pointer.allocateBytes(sz)
  }

  def parse(s: String): List[MecabResult] = {
    val arr = s.getBytes("UTF-8")
    val needed = arr.length
    if (needed > bsize) {
      resize(needed * 6 / 5) //needed * 1.2
    }
    buffer.setBytes(arr)
    val node = MecabLibrary.mecab_sparse_tonode2(tagger, buffer, needed)
    new NodeIterator(node).toList
  }

  override def finalize() {
    MecabLibrary.mecab_destroy(tagger)
    buffer.release()
  }
}
