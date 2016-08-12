package ws.kotonoha.akane.utils.timers

final class Millitimer {
  private def time() = System.currentTimeMillis()
  val start = time()
  def eplaced = Millis(time() - start)
}

final case class Millis(millis: Long) extends AnyVal {
  @inline def seconds = millis * 1e-3
  override def toString: String = millis.toString
}

object Millitimer {
  def apply() = new Millitimer
}

final class Nanotimer {
  val start = System.nanoTime()
  def eplaced = System.nanoTime() - start

  @inline def millis = f"${eplaced * 1e-6}%.3f"
}

object Nanotimer {
  def apply() = new Nanotimer
}
