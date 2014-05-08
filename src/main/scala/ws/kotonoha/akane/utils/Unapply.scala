package ws.kotonoha.akane.utils

object XInt {
  def unapply(x: String): Option[Int] = try { Some(x.toInt) } catch { case e: NumberFormatException => None }
}

object XDouble {
  def unapply(x: String): Option[Double] = try {
    Some(x.toDouble)
  } catch {
    case e: NumberFormatException => None
  }
}
