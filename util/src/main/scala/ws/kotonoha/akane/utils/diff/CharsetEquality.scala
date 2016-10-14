package ws.kotonoha.akane.utils.diff

import java.util

/**
  * @author eiennohito
  * @since 2015/10/13
  */
class CharsetEquality(characters: String, repCostSet: Int, repCost: Int, addCostSet: Int, addCost: Int) extends CharEquality {
  private val mySet = {
    val arr: Array[Int] = characters.codePoints().distinct().toArray
    util.Arrays.sort(arr)
    arr
  }

  private def contain(c: Int): Boolean = {
    val bsrch = util.Arrays.binarySearch(mySet, c)
    if (bsrch > 0) true else false
  }

  override def insertionCost(ch: Int) = {
    if (contain(ch)) {
      addCostSet
    } else {
      addCost
    }
  }

  override def replacementCost(ch1: Int, ch2: Int) = {
    if (contain(ch1) && contain(ch2)) {
      repCostSet
    } else {
      repCost
    }
  }

  override def areEqual(c1: Int, c2: Int) = c1 == c2
}
