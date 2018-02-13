package ws.kotonoha.akane.utils

import java.util

import ws.kotonoha.akane.utils.diff.SegmentBorders

/**
  * @author eiennohito
  * @since 15/08/13
  */
final class IntBuffer(private var arr: Array[Int] = Array.emptyIntArray) extends SegmentBorders {

  def this(capacity: Int) = {
    this(new Array[Int](capacity))
  }

  private var els = 0

  def ensureSize(sz: Int): Unit = {
    if (sz > arr.length) {
      arr = ArrayUtil.grow(arr, Math.max(sz, 12))
    }
  }

  def resize(sz: Int): Unit = {
    ensureSize(sz)
    els = sz
  }

  def append(i: Int): IntBuffer = {
    ensureSize(els + 1)
    arr(els) = i
    els += 1
    this
  }

  def append(r: Range): IntBuffer = {
    ensureSize(r.size)
    r.foreach(i => append(i))
    this
  }

  def apply(idx: Int): Int = arr(idx)

  def length: Int = els
  def size: Int = els

  def update(idx: Int, elem: Int): Unit = arr(idx) = elem

  def set(i: Int): Unit = {
    util.Arrays.fill(arr, i)
  }

  def elementsEquals(o: IntBuffer): Boolean = {
    util.Arrays.equals(arr, o.arr)
  }

  override def toString: String = {
    val sb = new StringBuilder
    var i = 0
    sb.append("[")
    while (i < els) {
      sb.append(arr(i))
      sb.append(", ")
      i += 1
    }
    val len = sb.length - 2
    sb.replace(len.max(1), sb.length, "]")
    sb.toString()
  }

  override def hashCode() = util.Arrays.hashCode(arr)

  override def equals(obj: scala.Any) = obj match {
    case o: IntBuffer => elementsEquals(o)
    case _            => false
  }
}

object IntBuffer {
  def empty = new IntBuffer()

  def apply(r: Range): IntBuffer = {
    val buf = new IntBuffer(r.length)
    buf.append(r)
    buf
  }

}
