package ws.kotonoha.akane.kytea

import ws.kotonoha.akane.kytea.wire.{KyteaSentence, KyteaUnit}

/**
  * @author eiennohito
  * @since 2016/09/12
  */
class KyteaRenderer(cfg: KyteaConfig) {
  def renderMorhpeme(bldr: Appendable, m: KyteaUnit): Unit = {
    val iter = m.fields.iterator
    while (iter.hasNext) {
      val o = iter.next()
      bldr.append(o)
      if (iter.hasNext) {
        bldr.append(cfg.tagBound)
      }
    }
  }

  def renderResult(bldr: Appendable, m: KyteaSentence): Unit = {
    val iter = m.units.iterator
    while (iter.hasNext) {
      val o = iter.next()
      renderMorhpeme(bldr, o)
      if (iter.hasNext) {
        bldr.append(cfg.wordBound)
      }
    }
  }

  def renderResult(m: KyteaSentence): String = {
    val bldr = new java.lang.StringBuilder()
    renderResult(bldr, m)
    bldr.toString
  }
}
