package ws.kotonoha.akane.pipe.knp.lisp

import java.io.InputStream
import java.nio.charset.Charset
import java.nio.{ByteBuffer, CharBuffer}

import ws.kotonoha.akane.utils.CalculatingIterator

import scala.util.parsing.input.{OffsetPosition, Reader}

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
