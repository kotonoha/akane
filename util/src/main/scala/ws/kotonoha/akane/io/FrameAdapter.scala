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

import java.io.InputStream
import java.nio.ByteBuffer

/**
  * @author eiennohito
  * @since 2015/10/08
  */
class FrameAdapter(inp: FramingBuffer) extends CloseableIterator[ByteBuffer] {
  private val buffer = new GrowableByteBuffer()

  override def close() = inp.close()

  private var calledNext = false

  override def next() = {
    calledNext = true
    if (inp.isEndVisible) {
      inp.data
    } else {
      buffer.reset()
      buffer.append(inp)
      buffer.asBuffer
    }
  }

  private var nextItem = inp.startFrame()

  override def hasNext = {
    if (calledNext) {
      calledNext = false
      inp.endFrame()
      nextItem = inp.startFrame()
    }

    nextItem
  }
}

object FrameAdapter {
  def apply(is: InputStream, delim: Array[Byte]) = {
    new FrameAdapter(new InputStreamFramingBuffer(is, delim))
  }
}
