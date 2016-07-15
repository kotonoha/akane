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
  * @since 2015/12/09
  */
class ByteBufferInputStream(buf: ByteBuffer) extends InputStream {
  override def read(): Int = {
    if (buf.remaining() == 0) return -1
    buf.get()
  }
  override def read(b: Array[Byte]): Int = {
    if (buf.remaining() == 0) return -1
    val pos = buf.position()
    val toRead = b.length min buf.remaining()
    buf.get(b, 0, toRead)
    toRead
  }
  override def read(b: Array[Byte], off: Int, len: Int): Int = {
    if (buf.remaining() == 0) return -1
    val pos = buf.position()
    val toRead = len min buf.remaining()
    buf.get(b, off, toRead)
    toRead
  }

  override def available() = {
    buf.remaining()
  }

  override def skip(n: Long) = {
    assert(n <= available())
    buf.position(buf.position() + n.toInt)
    n
  }
}
