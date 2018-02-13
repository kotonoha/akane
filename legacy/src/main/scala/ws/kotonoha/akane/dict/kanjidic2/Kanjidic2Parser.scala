/*
 * Copyright 2012 eiennohito
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

package ws.kotonoha.akane.dict.kanjidic2

import scalax.file.Path
import java.io.InputStream
import javax.xml.stream.XMLInputFactory
import ws.kotonoha.akane.xml._
import collection.mutable.ListBuffer

/**
  * @author eiennohito
  * @since 17.01.13
  */
case class KanjidicEntry(
    literal: String,
    codepoints: List[Entry],
    radicals: List[Entry],
    misc: Misc,
    dicRefs: List[DicRef],
    codes: List[Entry],
    rmgroups: List[RmGroup],
    nanori: List[String])

case class Misc(
    strokes: List[Int],
    variants: List[Entry],
    freq: Option[Int],
    grade: Option[Int],
    jlpt: Option[String])

case class LocString(data: String, lang: String)

case class DicRef(drType: String, pos: String, rest: Map[String, String] = Map())

case class Entry(name: String, value: String)

case class RmGroup(readings: List[Entry], meanings: List[LocString]) {
  lazy val onyomi: Seq[String] = readings.filter(_.name == KanjidicTypes.onyomi).map(_.value)
  lazy val kunyomi: Seq[String] = readings.filter(_.name == KanjidicTypes.kunyomi).map(_.value)
  //kunyomi without end of word marks
  lazy val cleanKunyomi: Seq[String] = kunyomi.map(_.replace(".", ""))
}

object KanjidicTypes {
  val kunyomi = "ja_kun"
  val onyomi = "ja_on"
  val pinyin = "pinyin"
  val korean_h = "korean_h"
}

object Kanjidic2Parser {

  import ws.kotonoha.akane.xml.XmlParser._

  def pasrse(file: Path) = {}

  private def isKanjidicNode(it: XmlData) = it match {
    case XmlEl("kanjidic2") => true
    case _                  => false
  }

  private def parseKvp(it: XmlParseTransformer, tag: String, key: String) = {
    it.head match {
      case t @ XmlEl(`tag`) => Entry(t(key), it.textOf(tag))
      case x =>
        throw new IllegalStateException(s"Tag ${x.data} should not been here, was waiting for $tag")
    }
  }

  private def parseCol(it: XmlParseTransformer, outer: String, inner: String, key: String) = {
    it.transOpt(outer) { it =>
        it.traverse(inner) {
            parseKvp(_, inner, key)
          }
          .toList
      }
      .getOrElse(Nil)
  }

  private def parseMisc(it: XmlParseTransformer) = {
    it.trans("misc") { it =>
      val scnt = new ListBuffer[Int]
      val vars = new ListBuffer[Entry]
      var grade, freq: Option[Int] = None
      var jlpt: Option[String] = None
      it.selector {
        case XmlEl("grade") =>
          grade = it.optTextOf("grade").map {
            _.toInt
          }
        case XmlEl("stroke_count") => scnt += it.textOf("stroke_count").toInt
        case XmlEl("variant")      => vars += parseKvp(it, "variant", "var_type")
        case XmlEl("freq") =>
          freq = it.optTextOf("freq").map {
            _.toInt
          }
        case XmlEl("jlpt") => jlpt = it.optTextOf("jlpt")
      }
      Misc(scnt.result(), vars.result(), freq, grade, jlpt)
    }
  }

  private def parseDref(it: XmlParseTransformer) = {
    it.transOpt("dic_number")(_.traverse("dic_ref") { it =>
        it.head match {
          case e @ XmlEl("dic_ref") =>
            DicRef(e("dr_type"), it.textOf("dic_ref"), e.attrs.toMap - "dr_type")
          case _ => throw new IllegalStateException()
        }
      }.toList)
      .getOrElse(Nil)
  }

  private def parseRm(it: XmlParseTransformer) = {
    it.transOpt("reading_meaning") { it =>
        val grps = it
          .transSeq("rmgroup") { it =>
            val rds = new ListBuffer[Entry]
            val mns = new ListBuffer[LocString]
            it.selector {
              case XmlEl("reading") => rds += parseKvp(it, "reading", "r_type")
              case x @ XmlEl("meaning") =>
                mns += LocString(it.textOf("meaning"), x.attrs.getOrElse("m_lang", "en"))
            }
            RmGroup(rds.result(), mns.result())
          }
          .toList
        val nanori = it
          .transSeq("nanori") {
            _.textOf("nanori")
          }
          .toList
        (grps, nanori)
      }
      .getOrElse((Nil, Nil))
  }

  def parseEntry(it: XmlParseTransformer): KanjidicEntry = {
    val literal = it.textOf("literal")
    val cps = parseCol(it, "codepoint", "cp_value", "cp_type")
    val rads = parseCol(it, "radical", "rad_value", "rad_type")
    val misc = parseMisc(it)
    val refs = parseDref(it)
    val codes = parseCol(it, "query_code", "q_code", "qc_type")
    val (rms, nanori) = parseRm(it)
    KanjidicEntry(literal, cps, rads, misc, refs, codes, rms, nanori)
  }

  def parse(stream: InputStream): Iterator[KanjidicEntry] = {
    val fact = XMLInputFactory.newInstance()
    fact.setProperty(XMLInputFactory.IS_VALIDATING, false)
    fact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false)

    val reader =
      fact.createFilteredReader(fact.createXMLEventReader(stream, "UTF-8"), WhitespaceFilter)
    val parser = XmlParser.parse(reader)

    while (parser.hasNext && !isKanjidicNode(parser.next())) {}

    val entries = parser.transSeq("character") { it =>
      parseEntry(it)
    }
    entries
  }

}
