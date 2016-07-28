/*
 * Copyright 2016 eiennohito (Tolmachev Arseny)
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

package ws.kotonoha.akane.io

import java.io.{BufferedReader, InputStream, InputStreamReader}
import java.nio.charset.Charset

/**
  * @author eiennohito
  * @since 2016/07/27
  */
class LineIterator(rdr: BufferedReader) extends CloseableIterator[String] {
  private[this] var nextLine = readLine()

  protected def readLine(): String = {
    rdr.readLine()
  }

  override def hasNext = nextLine != null

  override def next() = {
    val line = nextLine
    nextLine = readLine()
    line
  }

  override def close() = rdr.close()
}

object LineIterator {
  def apply(rdr: BufferedReader): LineIterator = new LineIterator(rdr)
  def apply(is: InputStream, charset: Charset = Charsets.utf8): LineIterator = {
    val rdr = new BufferedReader(new InputStreamReader(is, charset))
    apply(rdr)
  }
}
