package ws.kotonoha.akane.parser

import collection.mutable.ListBuffer

class DebugInput(inner: AozoraInput) extends AozoraInput {
  private val lb = new ListBuffer[Char]
  private var line = 0
  private var pos = 0

  def peek = inner.peek

  //doesn't go forward
  def next = {
    val prev = peek
    if (lb.size == 20) {
      lb.remove(0, 1)
    }
    lb += prev.toChar
    if (prev == 10) {
      line += 1
      pos = 0
    } else {
      pos += 1
    }
    inner.next
  }

  override def toString = "(%d, %d), last 20 chars: %s".format(line, pos, lb.mkString(""))

  def mark() = inner.mark()
  def subseq(rel: Int) = inner.subseq(rel)
}
