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

package ws.kotonoha.akane.io

import java.nio.ByteBuffer

/**
  * @author eiennohito
  * @since 2016/07/12
  */
class EosFramedIterator(adp: FrameAdapter) extends CloseableIterator[ByteBuffer] {
  override def close() = adp.close()

  override def next() = {
    val obj = adp.next()
    myBlocks += 1
    mySize += obj.remaining()
    obj
  }

  override def hasNext = adp.hasNext

  private var myBlocks = 0

  def blocks: Int = myBlocks

  private var mySize = 0L

  def readBytes: Long = mySize
}
