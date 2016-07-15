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

package ws.kotonoha.akane.pipe.juman

import akka.actor._
import ws.kotonoha.akane.juman.JumanUtil

sealed trait JumanMessage
case class JumanQuery(input: String) extends JumanMessage
case class DelayedJumanQuery(input: String, returnAdress: ActorRef) extends JumanMessage
case class ParsedQuery(inner : List[JumanEntry]) extends JumanMessage

case class JumanEntry(writing: String, reading: String, dictForm: String, spPart: String, comment: String) extends JumanMessage {
  @transient lazy val daihyou = JumanUtil.daihyouWriting(this)
  def tag(tag: String) = JumanUtil.extractTag(this, tag)
  def tags = JumanUtil.extractTags(this)
}

object JumanEntry {
  def parse(in: String) = {
    val tokens = in.split(' ')
    //val comment = if (in.count(_ == '"') != 2) "NIL" else in.dropWhile(_ != '"').drop(1).dropRight(2)
    val left = in.indexOf('\"')
    val comment = {
      if (left == -1) {
        "NIL"
      } else {
        in.substring(left + 1, in.indexOf('\"', left + 1))
      }
    }
    JumanEntry(tokens(0), tokens(1), tokens(2), tokens(3), comment)
  }
}

object JumanRW {
  def unapply(in: JumanEntry) = Some((in.writing, in.reading))
}

