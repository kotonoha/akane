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

import java.io.BufferedReader

/**
  * @author eiennohito
  * @since 2015/12/09
  */
class DelimetedIterator(val reader: BufferedReader, stop: String) extends BufferedIterator[String] {

  var string: String = reader.readLine()

  override def head = string

  override def next() = {
    val data = string
    string = reader.readLine()
    data
  }

  override def hasNext = reader.ready() && string != stop
}
