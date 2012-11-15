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

package ws.kotonoha.akane.mecab

import ws.kotonoha.akane.unicode.{KanaUtil, UnicodeUtil}
import ws.kotonoha.akane.utils.StringUtil
import ws.kotonoha.akane.basic.ReadingAndWriting

/**
 * @author eiennohito
 * @since 30.10.12 
 */
object InfoExtractor {
  def restoreWriting(dform: String, dread: String, variants: String) = {
    UnicodeUtil.hasKanji(dform) match {
      case true => dform
      case false => {
        val vars = variants.split('/')
        val ho = vars.toStream.filter(UnicodeUtil.hasKanji(_)).headOption
        ho match {
          case None => dform //have no kanji
          case Some(wr) => {
            val krd = KanaUtil.kataToHira(dread)
            val tail = StringUtil.commonTail(krd, wr)
            val len = dread.length - tail
            wr.substring(0, len) + dform.substring(len)
          }
        }
      }
    }
  }

  def restoreReading(df: String, nfo: String): String = {
    UnicodeUtil.hasKanji(df) match {
      case false => KanaUtil.kataToHira(df)
      case true => {
        val vars = nfo.split('/')
        val sorted = vars.map {
          w => (w, StringUtil.commonHead(w, df))
        }.sortBy(-_._2)
        val lst = sorted.headOption
        lst match {
          case Some((wr, head)) => {
            val kanavar = vars(0) //should be so
            if (head == wr.length) { //this means that writing is equals with info form
              kanavar + df.substring(head)
            } else {
              val tail = StringUtil.commonTail(wr.substring(head), kanavar)
              val h = head
              val s1 = kanavar.substring(0, kanavar.length - tail)
              s1 + df.substring(h)
            }
          }
          case None => ""
        }
      }
    }
  }

  case class DefaultMecabInfo (
    mecabNfo: MecabResult,
    pos: MecabPosInfo,
    dicForm: String,
    reading: String,
    normReading: String,
    jdicNfo: String,
    jdicXml: String,
    dformNfo: Option[String]
) extends MecabEntryInfo {
    def surface = mecabNfo.surf

    def calcWritingFromNfo = {
      if (jdicNfo.length == 0) None
      else {
        Some(restoreWriting(dicForm, reading, jdicNfo))
      }
    }

    lazy val dicWriting = {
      val v1 = dformNfo map(_.split("/")(0))
      v1 orElse calcWritingFromNfo
    }

    lazy val dicReading = {
      if (jdicNfo.length == 0) None
      else {
        Some(restoreReading(dicForm, jdicNfo))
      }
    }
  }

  //動詞,自立,*,*,五段・ラ行,仮定形,すみわたる,スミワタレ,スミワタレ

  def parseDefaultInfo(en: MecabResult, info: Array[String]): MecabEntryInfo = {
    val pinfo = info.take(6)
    val mpi = MecabPosInfo(pinfo: _*)
    val dform = info(6)
    val read = info(7)
    val readNorm = info(8)
    val jdicVars = info(9)
    val jdicMetaXml = info(10)
    val dformVars = if (info.length == 12) Some(info(11)) else None
    DefaultMecabInfo(en, mpi, dform, read, readNorm, jdicVars, jdicMetaXml, dformVars)
  }

  def parseModifiedInfo(en: MecabResult, info: Array[String]): MecabEntryInfo = {
    parseDefaultInfo(en, info)
  }

  val splitRe = "\\s*,\\s*".r

  case class UnkWordInfo(mr: MecabResult, pos: MecabPosInfo) extends MecabEntryInfo {
    def surface = mr.surf
    def dicForm = surface
    def dicWriting = None
    def dicReading = None
    def reading = surface
    def normReading = surface
  }

  def parseUnkWord(mr: MecabResult, nfo: Array[String]) = {
    UnkWordInfo(mr, MecabPosInfo(nfo.take(6): _*))
  }

  def extract(en: MecabResult, info: String) = {
    val nfo = splitRe.pattern.split(info, -1)
    nfo.length match {
      case 11 => parseDefaultInfo(en, nfo)
      case 12 => parseModifiedInfo(en, nfo)
      case 7 => parseUnkWord(en, nfo)
      case _ => throw new MalformattedInfoException(info)
    }
  }

  def parseInfo(entry: MecabResult): MecabEntryInfo = {
    extract(entry, entry.info)
  }
}

class MalformattedInfoException(info: String) extends RuntimeException(info)
