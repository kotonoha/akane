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

package ws.kotonoha.akane.dict.jmdict

/**
 * @author eiennohito
 * @since 14.11.12 
 */

case class LocString(str: String, loc: String)

case class Meaning(info: List[String], vals: List[LocString])

case class Priority(value: String)

case class JMString(priority: List[Priority], info: List[String], value: String)

case class JMRecord(id: Long, reading: List[JMString], writing: List[JMString], meaning: List[Meaning])

import ws.kotonoha.akane.xml._
import java.io.InputStream
import javax.xml.stream.XMLInputFactory
import collection.mutable.ListBuffer


object JMDictParser {

  import XmlParser._

  def isJmdicNode(x: XmlData) = x match {
    case XmlEl("JMdict") => true
    case _ => false
  }


  import scala.collection.{mutable => mut}

  def parseJmString(it: XmlParseTransformer, name: String, teb: String, tpri: String, inf: String): JMString = {
    it.trans(name) {
      it =>
        val v = it.textOf(teb)
        val pris = new ListBuffer[Priority]()
        val strs = new ListBuffer[String]()
        it.selector {
          case XmlEl(`tpri`) => pris += Priority(it.textOf(tpri))
          case XmlEl(`inf`) => strs += it.textOf(inf)
        }
        JMString(pris.result(), strs.result(), v)
    }
  }

  private def lang(n: XmlEl) = {
    n.attrs.get("lang") match {
      case Some(l) => l
      case None => "eng"
    }
  }

  def parseSense(it: XmlParseTransformer): Meaning = {
    it.trans("sense") {
      it =>
        val gl = new mut.ListBuffer[LocString]
        val poss = new mut.ListBuffer[String]
        val misc = new mut.ListBuffer[String]
        it.selector {
          case XmlEl("pos") => poss += it.textOf("pos") //rec.pos(JMDictAnnotations.safeValueOf(it.textOf("pos")))
          case XmlEl("misc") => misc += it.textOf("misc")
          case x@XmlEl("gloss") => gl += LocString(it.textOf("gloss"), lang(x))
        }
        Meaning(info = poss.toList ++ misc.toList, vals = gl.toList)
    }
  }

  def parse(stream: InputStream) = {
    val fact = XMLInputFactory.newInstance()
    fact.setProperty(XMLInputFactory.IS_VALIDATING, false)
    fact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false)

    val reader = fact.createFilteredReader(fact.createXMLEventReader(stream, "UTF-8"), WhitespaceFilter)
    val parser = XmlParser.parse(reader)
    while (parser.hasNext && !isJmdicNode(parser.next())) {}
    val entries = parser.transSeq("entry") {
      it =>
        parseEntry(it)
    }
    entries
  }

  def parseEntry(it: XmlParseTransformer): JMRecord = {
    val rds = new mut.ListBuffer[JMString]
    val wrs = new mut.ListBuffer[JMString]
    val mns = new mut.ListBuffer[Meaning]
    val id = it.textOf("ent_seq").toLong
    it.selector {
      case XmlEl("r_ele") => rds += parseJmString(it, "r_ele", "reb", "re_pri", "re_inf")
      case XmlEl("k_ele") => wrs += parseJmString(it, "k_ele", "keb", "ke_pri", "ke_inf")
      case XmlEl("sense") => mns += parseSense(it)
    }
    JMRecord(
      id = id,
      reading = rds.toList,
      writing = wrs.toList,
      meaning = mns.toList
    )
  }
}
