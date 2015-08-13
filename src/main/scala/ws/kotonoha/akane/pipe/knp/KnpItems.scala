package ws.kotonoha.akane.pipe.knp

import org.apache.commons.lang3.StringUtils
import ws.kotonoha.akane.ParseUtil
import ws.kotonoha.akane.parser.FeatureLocation

import scala.collection.mutable.ListBuffer

/**
 * @author eiennohito
 * @since 2013-09-04
 */
trait JapaneseLexeme extends FeatureLocation {
  def surface: String
  def reading: String
  def dicForm: String
  def pos: JumanPosInfo
  def info: String
  def tags: Seq[String]
  def canonicForm(): String

  override protected def featureSeq = tags
}

case class PosItem(name: String, id: Int)

case class JumanPosInfo(pos: PosItem, category: PosItem, conjType: PosItem, conjForm: PosItem)

//かわったり かわったり かわる 動詞 2 * 0 子音動詞ラ行 10 タ系連用タリ形 15 "代表表記:代わる/かわる 自他動詞:他:代える/かえる"
case class KnpLexeme(
                      surface: String,
                      reading: String,
                      dicForm: String,
                      pos: JumanPosInfo,
                      info: String,
                      tags: List[String]) extends JapaneseLexeme {

  def canonicForm(): String = findFeature("代表表記").getOrElse(s"$surface/$reading")
}

object KnpLexeme {
  val spaceRe = " ".r
  def fromTabFormat(line: CharSequence): KnpLexeme = {

    val end0 = 0
    val end1 = StringUtils.indexOf(line, ' ', end0)
    val end2 = StringUtils.indexOf(line, ' ', end1 + 1)
    val end3 = StringUtils.indexOf(line, ' ', end2 + 1)
    val end4 = StringUtils.indexOf(line, ' ', end3 + 1)
    val end5 = StringUtils.indexOf(line, ' ', end4 + 1)
    val end6 = StringUtils.indexOf(line, ' ', end5 + 1)
    val end7 = StringUtils.indexOf(line, ' ', end6 + 1)
    val end8 = StringUtils.indexOf(line, ' ', end7 + 1)
    val end9 = StringUtils.indexOf(line, ' ', end8 + 1)
    val end10 = StringUtils.indexOf(line, ' ', end9 + 1)
    val end11 = StringUtils.indexOf(line, ' ', end10 + 1)


    val rest0 = StringUtils.indexOf(line, '"', end11)
    val rest1 = StringUtils.indexOf(line, '"', rest0 + 1)

    val rest = if (rest0 < 0 || rest1 < 0) ""
               else line.subSequence(rest0 + 1, rest1).toString

    val bldr = new ListBuffer[String]

    var feStart = StringUtils.indexOf(line, '<', rest1)
    val length = line.length
    while (feStart > 0 && feStart < length) {
      val feEnd = StringUtils.indexOf(line, '>', feStart)
      if (feEnd > 0) {
        val feature = line.subSequence(feStart + 1, feEnd)
        bldr += feature.toString
        feStart = StringUtils.indexOf(line, '<', feEnd + 1)
      } else feStart = -1
    }

    KnpLexeme(
      line.subSequence(end0, end1).toString,
      line.subSequence(end1 + 1, end2).toString,
      line.subSequence(end2 + 1, end3).toString,
      JumanPosInfo(
        PosItem(
          line.subSequence(end3 + 1, end4).toString,
          ParseUtil.parseInt(line, end4 + 1, end5)
        ), PosItem(
          line.subSequence(end5 + 1, end6).toString,
          ParseUtil.parseInt(line, end6 + 1, end7)
        ), PosItem(
          line.subSequence(end7 + 1, end8).toString,
          ParseUtil.parseInt(line, end8 + 1, end9)
        ), PosItem(
          line.subSequence(end9 + 1, end10).toString,
          ParseUtil.parseInt(line, end10 + 1, end11)
        )
      ),
      rest,
      bldr.result()
    )
  }

  def fromTabFormatSlow(line: CharSequence): KnpLexeme = {
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
