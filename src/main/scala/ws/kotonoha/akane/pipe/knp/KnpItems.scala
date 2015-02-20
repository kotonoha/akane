package ws.kotonoha.akane.pipe.knp

import org.apache.commons.lang3.StringUtils

/**
 * @author eiennohito
 * @since 2013-09-04
 */
case class PosItem(name: String, id: Int)

case class JumanPosInfo(pos: PosItem, category: PosItem, conjType: PosItem, conjForm: PosItem)

//かわったり かわったり かわる 動詞 2 * 0 子音動詞ラ行 10 タ系連用タリ形 15 "代表表記:代わる/かわる 自他動詞:他:代える/かえる"
case class KnpLexeme(
                      surface: String,
                      reading: String,
                      dicForm: String,
                      pos: JumanPosInfo,
                      info: String,
                      tags: List[String]) {

  def findFeature(feature: String) = {
    (for (t <- this.tags if t.startsWith(feature)) yield t).headOption.map {
      s =>
        if (s.length > feature.length && s.charAt(feature.length) == ':')
          s.substring(feature.length + 1)
        else Some("")
    }
  }
}

object KnpLexeme {
  val spaceRe = " ".r
  def fromTabFormat(line: CharSequence) = {
    val fields = spaceRe.pattern.split(line, 12)
    val rest = fields(11)
    val featuresBegin = rest.indexOf('<')
    val info = StringUtils.strip(rest.substring(0, featuresBegin - 1), " \"")
    val features = rest.substring(featuresBegin).split("><").map(s => StringUtils.strip(s, ">< "))
    KnpLexeme(fields(0), fields(1), fields(2),
      JumanPosInfo(
        PosItem(fields(3), fields(4).toInt),
        PosItem(fields(5), fields(6).toInt),
        PosItem(fields(7), fields(8).toInt),
        PosItem(fields(9), fields(10).toInt)),
      info, features.toList)
  }
}

case class KnpItemRelation(to: Int, tags: List[String])

case class KnpItem(num: Int, star: KnpItemRelation, plus: KnpItemRelation, lexems: Seq[KnpLexeme])

case class KnpNode(num: Int, kind: String, surface: List[KnpLexeme], features: List[String], children: List[KnpNode] = Nil)
