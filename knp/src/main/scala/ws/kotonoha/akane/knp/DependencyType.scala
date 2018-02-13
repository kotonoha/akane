package ws.kotonoha.akane.knp

/**
  * @author eiennohito
  * @since 2016/11/30
  */
object DependencyType {
  val Normal = 1 //N per KNP
  val Parallel = 2 //P per KNP
  val Apposition = 3 //A per KNP
  val Incomplete = 4 //I per KNP

  def fromString(s: CharSequence): Int = fromChar(s.charAt(0))
  def fromChar(c: Char): Int = c match {
    case 'd' | 'D' => Normal
    case 'p' | 'P' => Parallel
    case 'a' | 'A' => Apposition
    case 'i' | 'I' => Incomplete
    case _         => 0xffff0000 | (c & 0xffff)
  }
}
