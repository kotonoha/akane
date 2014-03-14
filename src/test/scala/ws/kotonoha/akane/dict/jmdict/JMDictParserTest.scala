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

import javax.xml.stream.XMLInputFactory
import java.io.ByteArrayInputStream
import ws.kotonoha.akane.xml.{WhitespaceFilter, XmlParser, XmlParseTransformer}
import scalax.file.Path


class JMDictParserTest extends org.scalatest.FunSuite with org.scalatest.matchers.ShouldMatchers {
  import XmlParser._

  def p(s: String): XmlParseTransformer = {
    val fact = XMLInputFactory.newInstance()
    fact.setProperty(XMLInputFactory.IS_VALIDATING, false)
    fact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false)
    val ims = new ByteArrayInputStream(s.getBytes("UTF-8"))
    val reader = fact.createFilteredReader(fact.createXMLEventReader(ims, "UTF-8"), WhitespaceFilter)
    XmlParser.parse(reader)
  }

  test("jmstring parses successfully") {
    val d = p("""<k_ele>
    <keb>a</keb>
    </k_ele>""")
    val i = JMDictParser.parseJmString(d, "k_ele", "keb", "whatever", "none")
    i.value should equal ("a")
  }

  test("jmstring with priority parses successfully") {
    val d = p("""<k_ele>
    <keb>a</keb>
    <ke_pri>ichi2</ke_pri>
    </k_ele>""")
    val i = JMDictParser.parseJmString(d, "k_ele", "keb", "ke_pri", "none")
    i.value should equal ("a")
    i.priority.head should equal (Priority("ichi2"))
  }

  test("sense parses successfully") {
    val d = p("<sense>\n<pos>&n;</pos>\n<gloss>\"as above\" mark</gloss>\n<gloss xml:lang=\"ger\">(n) siehe oben (Abk.)</gloss>\n</sense>")
    val i = JMDictParser.parseSense(d)
    i.info should equal ("n" :: Nil)
    i.vals should have length (2)
  }

  test("one entry parses successfully") {
    val d = p("""<entry>
    <ent_seq>1000050</ent_seq>
    <k_ele>
    <keb>仝</keb>
    </k_ele>
    <r_ele>
    <reb>どうじょう</reb>
    </r_ele>
    <sense>
    <pos>&n;</pos>
    <gloss>"as above" mark</gloss>
    <gloss xml:lang="ger">(n) siehe oben (Abk.)</gloss>
    </sense>
    </entry>""")
    val e = d.trans("entry")(JMDictParser.parseEntry(_))
    val m = e.meaning
    m should have length (1)
    val m0 = m(0)
    m0.info should equal ("n" :: Nil)
    m0.vals should have size (2)
    e.reading.head.value should equal ("どうじょう")
  }

  test("jmdict entry with empty gloss parses successfully") {
    val testStr = """<entry>
                    |<ent_seq>1030340</ent_seq>
                    |<r_ele>
                    |<reb>エレガンス</reb>
                    |<re_pri>gai1</re_pri>
                    |</r_ele>
                    |<sense>
                    |<pos>&adj-na;</pos>
                    |<gloss>elegance</gloss>
                    |<gloss xml:lang="ger">(f) Eleganz</gloss>
                    |<gloss xml:lang="hun">választékosság</gloss>
                    |<gloss xml:lang="spa">elegante</gloss>
                    |<gloss xml:lang="spa"> </gloss>
                    |<gloss xml:lang="swe">stilfullhet</gloss>
                    |</sense>
                    |</entry>""".stripMargin
    val entry = p(testStr).trans("entry") (JMDictParser.parseEntry)
    val m = entry.meaning
    m should have length(1)
    m.head.vals should have length(5)
  }
}
