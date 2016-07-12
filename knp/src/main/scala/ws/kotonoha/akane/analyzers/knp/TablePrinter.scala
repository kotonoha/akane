package ws.kotonoha.akane.analyzers.knp

import ws.kotonoha.akane.analyzers.juman.{JumanOption, JumanPos}
import ws.kotonoha.akane.analyzers.knp.wire.KnpTable
import ws.kotonoha.akane.parser.JumanPosSet

/**
 * @author eiennohito
 * @since 2015/09/18
 */
class TablePrinter(posSet: JumanPosSet) {

  def appendPos(apd: Appendable, pos: JumanPos) = {
    val part = posSet.pos(pos.pos)
    val subpart = part.subtypes(pos.subpos)
    val conjType = posSet.conjugatons(pos.category)
    val conjuation = conjType.conjugations(pos.conjugation)
    apd.append(part.name).append(" ").append(part.num.toString).append(" ")
    apd.append(subpart.name).append(" ").append(subpart.num.toString).append(" ")
    apd.append(conjType.name).append(" ").append(conjType.num.toString).append(" ")
    apd.append(conjuation.name).append(" ").append(conjuation.num.toString).append(" ")
  }

  def appendFeatures(apd: Appendable, features: Seq[JumanOption]) = {
    for (f <- features) {
      apd.append("<")
      apd.append(f.key)
      f.value.foreach { v =>
        apd.append(":")
        apd.append(v)
      }
      apd.append(">")
    }
  }

  def appendTable(apd: Appendable, table: KnpTable) = {
    table.comment.foreach { c =>
      apd.append('#')
      apd.append(c)
      apd.append("\n")
    }
    var kStart = 0
    var lStart = 0
    for (b <- table.bunsetsuInfo) {
      apd.append("* ")
      apd.append(b.dependency.toString)
      apd.append(b.dependencyType)
      apd.append(" ")
      appendFeatures(apd, b.features)
      apd.append("\n")
      val kEnd = kStart + b.kihonkuCnt
      for (k <- table.kihonkuInfo.slice(kStart, kEnd)) {
        apd.append("+ ")
        apd.append(k.dependency.toString)
        apd.append(k.dependencyType)
        apd.append(" ")
        appendFeatures(apd, k.features)
        apd.append("\n")
        val lEnd = lStart + k.lexemeCnt
        for (l <- table.lexemes.slice(lStart, lEnd)) {
          apd.append(l.surface).append(" ")
          apd.append(l.reading).append(" ")
          apd.append(l.baseform).append(" ")
          appendPos(apd, l.posInfo)
          apd.append("NIL").append(" ")
          appendFeatures(apd, l.options)
          apd.append("\n")
        }
        lStart = lEnd
      }
      kStart = kEnd
    }
    apd.append("EOS\n")
  }
}
