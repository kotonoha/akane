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

case class JumanDaihyou(writing: String, reading: String)

object JumanUtil {
  private val regex = """代表表記:(.+?)/(.+?)\b""".r

  private val ignore = """^[- 　0-9０-９a-zA-Zａ-ｚＡ-Ｚ。、（）〈〉()\[\]……~～〜\\＝/"・?？!！——「」『』]+$""".r

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
    regex.findFirstMatchIn(ent.comment) match {
      case Some(Groups(wr, rd)) => JumanDaihyou(stripDa(wr, pos), stripDa(rd, pos))
      case _ => JumanDaihyou(stripDa(ent.dictForm, pos), "")
    }
  }

  def ignored(in: String) = {
    ignore.findFirstIn(in).isEmpty
  }
}
