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

package ws.kotonoha.akane.parser

class AozoraStringInput(cont: String) extends AozoraInput {
  var pos = 0
  val max = cont.length
  var mark_ : List[Int] = Nil

  def peek = {
    if (pos == max) -1 else cont(pos)
  }

  //doesn't go forward
  def next = {
    if (pos == max) {
      throw new IndexOutOfBoundsException("We got past our string")
    }
    val c = peek
    pos += 1
    c
  }

  def mark() = {
    mark_ = pos :: mark_
  }

  def subseq(rel: Int) = mark_ match {
    case x :: xs => {
      mark_ = xs
      if (x == -1) {
        None
      } else {
        Some(cont.substring(x, pos))
      }
    }
    case Nil => None
  }
}
