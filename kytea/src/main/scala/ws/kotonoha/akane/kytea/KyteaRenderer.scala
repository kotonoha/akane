package ws.kotonoha.akane.kytea

/**
  * @author eiennohito
  * @since 2016/09/12
  */
object KyteaRenderer {
  def renderMorhpeme(bldr: Appendable, m: KyteaMorpheme): Unit = {
    val iter = m.parts.iterator
    while (iter.hasNext) {
      val o = iter.next()
      bldr.append(o)
      if (iter.hasNext) {
        bldr.append(KyteaConfig.tagBound)
      }
    }
  }

  def renderResult(bldr: Appendable, m: KyteaResult): Unit = {
    val iter = m.morphemes.iterator
    while (iter.hasNext) {
      val o = iter.next()
      renderMorhpeme(bldr, o)
      if (iter.hasNext) {
        bldr.append(KyteaConfig.wordBound)
      }
    }
  }

  def renderResult(m: KyteaResult): String = {
    val bldr = new java.lang.StringBuilder()
    renderResult(bldr, m)
    bldr.toString
  }
}
