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

package ws.kotonoha.akane.juman

import ws.kotonoha.akane.JumanEntry
import scala.Some
import util.matching.Regex.Groups
import ws.kotonoha.akane.basic.ReadingAndWriting

case class JumanDaihyou(writing: String, reading: String) extends ReadingAndWriting

case class JumanTag(tag: String, kind: String, value: String)

object JumanUtil {

  def extractTag(entry: JumanEntry, tag: String): Option[JumanTag] = {
    val c = entry.comment
    val i = c.indexOf(tag)
    if (i == -1) None
    else {
      val e = c.indexOf(' ', i)
      val x = if (e == -1) c.substring(i) else c.substring(i, e)
      x.split(":") match {
        case Array(tag, tp, cnt) => Some(JumanTag(tag, tp, cnt))
        case _ => None
      }
    }
  }

  val tagRe = """([^ ]+):([^ ]+):([^ ]+)""".r

  def extractTags(entry: JumanEntry) = {
    val x = tagRe.findAllMatchIn(entry.comment) map {
      case Groups(tag, tp, cnt) => JumanTag(tag, tp, cnt)
    }
    x.toList
  }

  private val daihyoRE = """代表表記:(.+?)/(.+?)\b""".r

  private val ignore =
    """^[- 　0-9０-９a-zA-Zａ-ｚＡ-Ｚ一二三四五六七八九十百千万億ゲケヶ
      | 。、（）〈〉()\[\]……~～〜\\＝/"・?？!！——「」『』]+$""".stripMargin.r

  def stripDa(str: String, pos: String): String = {
    if (pos.contains("形容") || pos.equals("助動詞")) {
      if (str.endsWith("だ")) {
        return str.substring(0, str.length - 1)
      }
    }
    str
  }

  def daihyouWriting(ent: JumanEntry) = {
    val pos = ent.spPart
    daihyoRE.findFirstMatchIn(ent.comment) match {
      case Some(Groups(wr, rd)) => JumanDaihyou(stripDa(wr, pos), stripDa(rd, pos))
      case _ => JumanDaihyou(stripDa(ent.dictForm, pos), "")
    }
  }

  def ignored(in: String) = {
    ignore.findFirstIn(in).isEmpty
  }
}
