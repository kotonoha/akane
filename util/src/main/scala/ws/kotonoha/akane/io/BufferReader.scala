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
 * @since 15/08/11
 */
final class BufferReader(buffer: ByteBuffer, ratio: Double = 0.1) {

  val refresh = (buffer.capacity() * ratio).toInt

  buffer.position(buffer.capacity())

  var eof = false

  private var totalRead = 0L

  @inline
  def fillFrom[T](stream: InputStream)(func: ByteBuffer => T): T = {
    if (buffer.remaining() < refresh && !eof) {
      buffer.compact()
      val read = stream.read(buffer.array(), buffer.position(), buffer.remaining())
      if (read == -1) {
        eof = true
        buffer.flip()
      } else {
        totalRead += read
        val pos = buffer.position()
        buffer.position(0)
        buffer.limit(pos + read)
      }
    }
    func(buffer)
  }

  def readBytes = totalRead
}
