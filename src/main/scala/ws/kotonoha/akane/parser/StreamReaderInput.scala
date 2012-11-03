package ws.kotonoha.akane.parser

import java.io.InputStreamReader
import java.nio.CharBuffer

/**
 * @author eiennohito
 * @since 17.08.12
 */

class StreamReaderInput(in: InputStreamReader) extends AozoraInput {
  private val charbuf = new Array[Char](8 * 4096)
  private var cur = 0
  private var end = 0
  private var mark_ : List[Int] = Nil
  private var eof = false
  var canread = true

  def drop(sz: Int = 2048): Unit = {
    assert(cur >= sz, "cur should be greater than sz")
    System.arraycopy(charbuf, sz, charbuf, 0, charbuf.length - sz)
    cur -= sz
    end -= sz
    mark_ = mark_ map { m => (m - sz) max (-1) }
  }

  end = in.read(charbuf)

  def peek = {
    if (eof) {
      -1
    } else {
      charbuf(cur)
    }
  }

  def mark() = {
    mark_ = cur :: mark_
  }

  class MyString(val cur: Int, val len: Int) extends CharSequence {
    def charAt(index: Int) = {
      if (index >= len) throw new IndexOutOfBoundsException
      charbuf(cur + index)
    }
    def length() = len
    def subSequence(start: Int, end: Int) = new MyString(cur + start, len - (end - start))
  }

  def subseq(rel: Int) = {
    mark_ match {
      case x :: xs => {
        mark_ = xs
        if (x == -1) {
          None
        } else {
          Some(new MyString(x, cur - x))
        }
      }
      case Nil => None
    }
  }

  //doesn't go forward
  def next: Int = {
    val c = peek
    if (!eof) {
      cur += 1
      if (cur == end) {
        eof = true
      } else if (cur >= 6 * 4096 && canread) {
        drop(2 * 4096)
        val howmany = charbuf.length - end
        val total = in.read(charbuf, end, howmany)
        if (total != howmany) {
          canread = false
        }
        end += total
      }
    }
    c
  }
}
