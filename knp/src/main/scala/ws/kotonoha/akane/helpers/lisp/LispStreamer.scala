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

package ws.kotonoha.akane.helpers.lisp

import java.io.InputStream
import java.nio.charset.Charset
import java.nio.{ByteBuffer, CharBuffer}

import ws.kotonoha.akane.utils.CalculatingIterator

/**
  * @author eiennohito
  * @since 2015/12/14
  */
class LispStreamer(data: InputStream) extends CalculatingIterator[KElement] {
  private val buffer = ByteBuffer.allocate(32 * 1024)
  private val chars = CharBuffer.allocate(16 * 1024)
  private val coder = Charset.forName("utf-8").newDecoder()
  private var EOF = false

  chars.position(chars.limit())
  buffer.limit(0)

  private def shouldRead(): Boolean = {
    !EOF && chars.remaining() < 2000
  }

  private def readInput(): Unit = {
    chars.compact()

    var toRead = chars.remaining() - buffer.position()

    while (toRead > 2000) {
      val readLen = data.read(buffer.array(), buffer.position(), toRead)

      if (readLen == -1) {
        EOF = true
        assert(buffer.position() == 0)
        chars.flip()
        return
      }

      toRead -= readLen

      buffer.limit(buffer.position() + readLen)
      buffer.position(0)

      coder.decode(buffer, chars, false)
      buffer.compact()
    }

    chars.flip()
  }

  def prepareInput() = {
    if (shouldRead()) {
      readInput()
    }
    new CharBufferWrappedSequence(chars)
  }

  private val sexpp = new SexpParser

  override protected def calculate(): Option[KElement] = {
    if (EOF && chars.remaining() < 10) {
      return None
    }
    val input = prepareInput()
    sexpp.parseExpression(input, chars.position()) match {
      case ParseSuccess(elm, pos) =>
        chars.position(pos)
        Some(elm)
      case ParseFailure(msg, pos) =>
        println(msg)
        None
    }
  }

  override def toString() = EOF.toString
}

class CharBufferWrappedSequence(cb: CharBuffer) extends CharSequence {
  override def charAt(index: Int) = cb.get(index)
  override def length() = cb.limit()
  override def subSequence(start: Int, end: Int) = {
    val lim = cb.limit()
    val pos = cb.position()
    cb.position(start)
    cb.limit(end)
    val res = new CharBufferWrappedSequence(cb.slice())
    cb.limit(lim)
    cb.position(pos)
    res
  }

  override def toString = cb.toString
}
