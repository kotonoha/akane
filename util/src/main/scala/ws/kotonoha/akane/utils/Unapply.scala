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

package ws.kotonoha.akane.utils

object XInt {
  def unapply(x: String): Option[Int] = try { Some(x.toInt) } catch {
    case e: NumberFormatException => None
  }
}

object XLong {
  def unapply(x: String): Option[Long] =
    try {
      Some(x.toLong)
    } catch {
      case e: NumberFormatException => None
    }
}

object XDouble {
  def unapply(x: String): Option[Double] =
    try {
      Some(x.toDouble)
    } catch {
      case e: NumberFormatException => None
    }
}
