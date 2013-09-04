package ws.kotonoha.akane.utils

object XInt {
  def unapply(x: String): Option[Int] = try { Some(x.toInt) } catch { case e: NumberFormatException => None }
}
