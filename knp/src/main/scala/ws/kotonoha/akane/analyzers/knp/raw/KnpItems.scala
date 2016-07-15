/*
 * Copyright 2012-2016 eiennohito (Tolmachev Arseny)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ws.kotonoha.akane.analyzers.knp.raw

import org.apache.commons.lang3.StringUtils
import ws.kotonoha.akane.analyzers.juman.JumanStylePos
import ws.kotonoha.akane.analyzers.knp.LexemeApi
import ws.kotonoha.akane.utils.ParseUtil

import scala.collection.mutable.ListBuffer





@deprecated("use protobuf-based apis", "0.3")
case class PosItem(name: String, id: Int)

@deprecated("use protobuf-based apis", "0.3")
case class JumanPosInfo(partOfSpeech: PosItem, subPart: PosItem, conjType: PosItem, conjForm: PosItem) extends JumanStylePos {
  override def pos = partOfSpeech.id
  override def subpos = subPart.id
  override def category = conjType.id
  override def conjugation = conjForm.id
}

//かわったり かわったり かわる 動詞 2 * 0 子音動詞ラ行 10 タ系連用タリ形 15 "代表表記:代わる/かわる 自他動詞:他:代える/かえる
case class OldAndUglyKnpLexeme (
                      surface: String,
                      reading: String,
                      dicForm: String,
                      pos: JumanPosInfo,
                      info: String,
                      tags: List[String]) extends LexemeApi with FeatureLocation {

  def canonicForm(): String = valueOfFeature("代表表記").getOrElse(s"$surface/$reading")

  override protected def featureSeq = tags
}

object OldAndUglyKnpLexeme {
  val spaceRe = " ".r
  def fromTabFormat(line: CharSequence): OldAndUglyKnpLexeme = {

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

    OldAndUglyKnpLexeme(
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

  @deprecated("use protobuf-based apis", "0.3")
  def fromTabFormatSlow(line: CharSequence): OldAndUglyKnpLexeme = {
    val fields = spaceRe.pattern.split(line, 12)
    val rest = fields(11)
    val featuresBegin = rest.indexOf('<')
    val info = StringUtils.strip(rest.substring(0, featuresBegin - 1), " \"")
    val features = rest.substring(featuresBegin).split("><").map(s => StringUtils.strip(s, ">< "))
    OldAndUglyKnpLexeme(fields(0), fields(1), fields(2),
      JumanPosInfo(
        PosItem(fields(3), fields(4).toInt),
        PosItem(fields(5), fields(6).toInt),
        PosItem(fields(7), fields(8).toInt),
        PosItem(fields(9), fields(10).toInt)),
      info, features.toList)
  }
}

@deprecated("use protobuf-based apis", "0.3")
case class KnpItemRelation(to: Int, tags: List[String])

@deprecated("use protobuf-based apis", "0.3")
case class KnpItem(num: Int, star: KnpItemRelation, plus: KnpItemRelation, lexems: Seq[OldAndUglyKnpLexeme])

@deprecated("use protobuf-based apis", "0.3")
case class KnpNode(num: Int, kind: String, surface: List[OldAndUglyKnpLexeme], features: List[String], children: List[KnpNode] = Nil)
