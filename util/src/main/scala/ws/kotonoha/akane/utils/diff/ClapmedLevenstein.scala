package ws.kotonoha.akane.utils.diff

import ws.kotonoha.akane.utils.IntBuffer

/**
  * @author eiennohito
  * @since 2015/10/13
  */
trait CharEquality {
  def insertionCost(ch: Int): Int
  def replacementCost(ch1: Int, ch2: Int): Int
  def areEqual(c1: Int, c2: Int): Boolean
}

class SimpleCharEquality extends CharEquality {
  override def areEqual(c1: Int, c2: Int) = c1 == c2
  override def insertionCost(ch: Int) = 1
  override def replacementCost(ch1: Int, ch2: Int) = 1
}

object CharEquality {
  val simple = new SimpleCharEquality
}

class ClapmedLevenstein(maxDist: Int, equality: CharEquality = CharEquality.simple) {
  private val a1 = new IntBuffer
  private val a2 = new IntBuffer

  def ensure(s1: CharSequence): Unit = {
    a1.resize(s1.length() + 1)
    a2.resize(s1.length() + 1)
  }

  private def doCompute(s1: CharSequence, s2: CharSequence): Int = {
    require(s1.length() <= s2.length())

    var cur = a1
    var prev = a2

    ensure(s1)

    val theend = s2.length()

    val maxEnd = s1.length()
    prev(0) = 0
    var j = 1
    while (j <= maxEnd) {
      prev(j) = prev(j - 1) + equality.insertionCost(s1.charAt(j - 1))
      cur(j) = prev(j)
      j += 1
    }

    var minCost = s1.length()

    var pos = 1
    while (pos <= theend) {

      val leftMargin = pos - maxDist

      if (leftMargin > s1.length()) {
        return minCost + s2.length() - s1.length()
      }

      val start = 1.max(leftMargin)
      val end = s1.length().min(pos + maxDist)

      //println(s"check $pos: [$start, $end]")

      minCost = Int.MaxValue

      var i = start
      val ch = s2.charAt(pos - 1)

      cur(start - 1) = prev(start - 1) + equality.insertionCost(ch)

      while (i <= end) {
        val ch2 = s1.charAt(i - 1)
        val prevPrevCost = prev(i - 1)

        if (equality.areEqual(ch, ch2)) {
          cur(i) = prevPrevCost
        } else {
          val ifDel = cur(i - 1) + equality.insertionCost(ch2)
          val ifAdd = prev(i) + equality.insertionCost(ch)
          val ifRep = prevPrevCost + equality.replacementCost(ch, ch2)
          cur(i) = ifDel.min(ifRep).min(ifAdd)
        }

        if (cur(i) < minCost) {
          minCost = cur(i)
        }

        i += 1
      }

      //println("last:" + prev)
      //println("cur: " + cur)

      if (pos >= maxDist) {
        val maxVal = minCost
        if (maxVal >= maxDist) return maxVal
      }

      val tmp = prev
      prev = cur
      cur = tmp

      pos += 1
    }

    //it does swap, so cur is prev
    prev(s1.length())
  }

  def distance(s1: CharSequence, s2: CharSequence): Int = {
    if (s1.length() > s2.length()) {
      doCompute(s2, s1)
    } else {
      doCompute(s1, s2)
    }
  }

}
