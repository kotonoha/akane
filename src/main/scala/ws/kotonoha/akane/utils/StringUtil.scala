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

/**
 * @author eiennohito
 * @since 30.10.12 
 */

object StringUtil {
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
}
