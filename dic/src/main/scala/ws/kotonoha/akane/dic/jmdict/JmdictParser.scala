/*
 * Copyright 2016 eiennohito (Tolmachev Arseny)
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

package ws.kotonoha.akane.dic.jmdict

import java.io.InputStream
import javax.xml.stream.XMLInputFactory

import org.apache.commons.lang3.StringUtils
import ws.kotonoha.akane.utils.XInt
import ws.kotonoha.akane.xml._

import scala.collection.mutable.ArrayBuffer


/**
  * @author eiennohito
  * @since 2016/07/20
  */
class JmdictParser {
  import ws.kotonoha.akane.xml.XmlParser._
  import JmdictParser._

  def isJmdicNode(x: XmlData) = x match {
    case XmlEl("JMdict") => true
    case _ => false
  }

  def parse(stream: InputStream): Iterator[JmdictEntry] = {
    val fact = XMLInputFactory.newInstance()
    fact.setProperty(XMLInputFactory.IS_VALIDATING, false)
    fact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false)

    val reader = fact.createFilteredReader(
      fact.createXMLEventReader(stream, "utf-8"),
      WhitespaceFilter
    )

    val parser = XmlParser.parse(reader)
    while (parser.hasNext && isJmdicNode(parser.next())) {} //skip until jmdic node
    val entries = parser.transSeq("entry") {
      it => parseEntry(it)
    }
    entries
  }

  private[jmdict] def parseKanji(it: XmlParseTransformer): KanjiInfo = {
    it.trans("k_ele") { it =>
      var data = ""
      var freq: Option[Int] = None
      val tags = new ArrayBuffer[JmdictTag]()
      val prio = new ArrayBuffer[Priority]()

      it.selector {
        case x@XmlEl("keb") => data = it.content()
        case x@XmlEl("ke_inf") =>
          tag(it.content()).foreach(tags += _)
        case x@XmlEl("ke_pri") =>
          it.content() match {
            case i if i.startsWith("nf") => freq = Some(i.substring(2).toInt)
            case i => priorities.get(i).foreach(prio += _)
          }
      }

      KanjiInfo(
        content = data,
        info = tags,
        priority = prio,
        freq = freq
      )
    }
  }

  def parseGloss(it: XmlParseTransformer): LocalizedString = {
    val (cont, attrs) = it.contAttrs()
    val lang = attrs.getOrElse("lang", "eng")
    LocalizedString(
      lang = lang,
      str = cont
    )
  }


  def parseXref(it: XmlParseTransformer): CrossReference = {
    val data = it.content()
    StringUtils.split(data, '・') match {
      case Array(wr, smt) =>
        XInt.unapply(smt) match {
          case Some(i) => CrossReference(wr, None, Some(i))
          case _ => CrossReference(wr, Some(smt))
        }
      case Array(wr, rd, id) =>
        CrossReference(wr, Some(rd), Some(id.toInt))
    }
  }

  def parseSource(it: XmlParseTransformer): SourceInfo = {
    val (data, attrs) = it.contAttrs()
    val cont = if (data != null && data != "") Some(data) else None
    val lang = attrs.getOrElse("lang", "eng")
    val part = attrs.get("ls_type").isDefined
    val wasei = attrs.get("ls_wasei").isDefined

    SourceInfo(cont, wasei, part, lang)
  }

  private[jmdict] def parseSense(it: XmlParseTransformer, ki: ArrayBuffer[KanjiInfo], ri: ArrayBuffer[ReadingInfo]): MeaningInfo = {
    it.trans("sense") { it =>
      val pos = new ArrayBuffer[JmdictTag]()
      val info = new ArrayBuffer[JmdictTag]()
      val cont = new ArrayBuffer[LocalizedString]()
      val kres = new ArrayBuffer[Int]()
      val rres = new ArrayBuffer[Int]()
      val xref = new ArrayBuffer[CrossReference]()
      val ant = new ArrayBuffer[CrossReference]()
      val src = new ArrayBuffer[SourceInfo]()
      val remarks = new ArrayBuffer[String]()

      it.selector {
        case x@XmlEl("gloss") => cont += parseGloss(it)
        case x@XmlEl("stagk") =>
          val data = it.content()
          val idx = ki.indexWhere(_.content == data)
          if (idx != -1) {
            kres += idx
          }
        case x@XmlEl("stagr") =>
          val data = it.content()
          val idx = ri.indexWhere(_.content == data)
          if (idx != -1) {
            rres += idx
          }
        case x@XmlEl("pos") => tag(it.content()).foreach(pos += _)
        case x@XmlEl("field" | "misc" | "dial") => tag(it.content()).foreach(info += _)
        case x@XmlEl("xref") => xref += parseXref(it)
        case x@XmlEl("ant") => ant += parseXref(it)
        case x@XmlEl("lsource") => src += parseSource(it)
        case x@XmlEl("s_inf") => remarks += it.content()
        case x@XmlEl("example") => it.skipTag()
      }

      MeaningInfo(
        pos = pos,
        info = info,
        content = cont,
        kanjiRestriction = kres,
        readingRestriction = rres,
        xref = xref,
        antonym = ant,
        source = src,
        remark = remarks
      )
    }
  }

  private[jmdict] def parseReading(it: XmlParseTransformer, ki: ArrayBuffer[KanjiInfo]): ReadingInfo = {
    it.trans("r_ele") { it =>

      var data = ""
      var nokanji = false
      var freq: Option[Int] = None
      val tags = new ArrayBuffer[JmdictTag]()
      val prio = new ArrayBuffer[Priority]()
      val restr = new ArrayBuffer[Int]()

      it.selector {
        case x@XmlEl("reb") => data = it.content()
        case x@XmlEl("re_nokanji") => it.skipTag(); nokanji = true
        case x@XmlEl("re_restr") =>
          val data = it.content()
          val idx = ki.indexWhere(_.content == data)
          if (idx != -1) {
            restr += idx
          }
        case x@XmlEl("re_inf") =>
          tag(it.content()).foreach(tags += _)
        case x@XmlEl("re_pri") =>
          it.content() match {
            case i if i.startsWith("nf") => freq = Some(i.substring(2).toInt)
            case i => priorities.get(i).foreach(prio += _)
          }
      }

      ReadingInfo(
        content = data,
        nokanji = nokanji,
        restr = restr,
        info = tags,
        priority = prio,
        freq = freq
      )
    }
  }

  private[jmdict] def parseEntry(it: XmlParseTransformer): JmdictEntry = {
    var id = 0L
    val ki = new ArrayBuffer[KanjiInfo]()
    val ri = new ArrayBuffer[ReadingInfo]()
    val mi = new ArrayBuffer[MeaningInfo]()
    it.selector {
      case x@XmlEl("ent_seq") => id = it.content().toLong
      case XmlEl("k_ele") => ki += parseKanji(it)
      case XmlEl("r_ele") => ri += parseReading(it, ki)
      case XmlEl("info") => it.skipTag() //skip for the time being
      case XmlEl("sense") => mi += parseSense(it, ki, ri)
    }
    JmdictEntry(
      id = id,
      readings = ri,
      writings = ki,
      meanings = mi,
      info = None
    )
  }
}

object JmdictParser {
  val priorities: Map[String, Priority] = Priority.values.map { v => v.name -> v }.toMap
  def tag(s: String): Option[JmdictTag] = JmdictTagMap.tagMap.get(s)
}
