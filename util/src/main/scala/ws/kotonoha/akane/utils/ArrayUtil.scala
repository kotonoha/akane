package ws.kotonoha.akane.utils

import java.util

/**
  * @author eiennohito
  * @since 2016/10/14
  */
object ArrayUtil {
  def grow(arr: Array[Int], toSize: Int): Array[Int] = {
    val curSize = arr.length
    val newSize = (curSize * 6 / 5) max toSize
    util.Arrays.copyOf(arr, newSize)
  }
}
