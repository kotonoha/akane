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

package ws.kotonoha.akane.utils

import java.nio.ByteBuffer
import java.nio.charset.Charset

/**
  * @author eiennohito
  * @since 30.10.12
  */
object StringUtil {
  def fromByteBuffer(v1: ByteBuffer, cs: Charset): String = {
    if (v1.hasArray) {
      val array = v1.array()
      new String(array, v1.arrayOffset() + v1.position(), v1.remaining(), cs)
    } else {
      val arr = new Array[Byte](v1.remaining())
      val pos = v1.position()
      v1.get(arr)
      v1.position(pos)
      new String(arr, cs)
    }
  }

  def commonTail(s1: String, s2: String): Int = {
    var l1 = s1.length - 1
    var l2 = s2.length - 1
    var continue = true
    while (l1 >= 0 && l2 >= 0 && continue) {
      if (s1(l1) == s2(l2)) {
        l1 -= 1
        l2 -= 1
      } else {
        continue = false
      }
    }
    s1.length - l1 - 1
  }

  def commonHead(s1: String, s2: String): Int = {
    val rest = s1.length.min(s2.length)
    var i = 0
    var cont = true
    while (i < rest && cont) {
      if (s1(i) == s2(i)) {
        i += 1
      } else {
        cont = false
      }
    }
    i
  }

  def isHeadOf(needle: CharSequence, haystack: CharSequence, pos: Int): Boolean = {
    val rest = needle.length().min(haystack.length() - pos)
    var i = 0
    while (i < rest) {
      val c1 = needle.charAt(i)
      val c2 = haystack.charAt(pos + i)
      if (c1 != c2) return false
      i += 1
    }
    i == needle.length()
  }

  def indexOfAny(seq: CharSequence, str: String, start: Int = 0): Int = {
    var i = start
    val seql = seq.length()
    val strl = str.length
    while (i < seql) {
      val c = seq.charAt(i)

      var j = 0
      while (j < strl) {
        val c2 = str.charAt(j)

        if (c == c2) return i

        j += 1
      }

      i += 1
    }
    -1
  }
}
