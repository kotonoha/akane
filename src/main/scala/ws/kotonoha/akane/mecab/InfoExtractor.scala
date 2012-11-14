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

  case class JdicMecabInfo (
                      pos: String,
                      cat1: String,
                      cat2: String,
                      form: String,
                      dictForm: String,
                      instanceReading: String,
                      normalizedReading: String,
                      variants: String,
                      metadata: String
                      ) extends ReadingAndWriting {
    lazy val reading = restoreReading(dictForm, variants)
    lazy val writing = restoreWriting(dictForm, instanceReading, normalizedReading)
  }

  //動詞,自立,*,*,五段・ラ行,未然形,すりきる,スリキラ,スリキラ,すりきら/摺り切ら/摺切ら/擦り切ら/擦切ら,
  def extractJdicInfo(strings: Array[String]) = {
    val pos = strings(0)
    val cat1 = strings(1)
    val cat2 = strings(4)
    val form = strings(5)
    val dform = strings(6)
    val dread = strings(7)
    val dreadnorm = strings(8)
    val writevariants = strings(9)
    val metadata = strings(10)
    JdicMecabInfo (
      pos, cat1, cat2,
      form, dform, dread,
      dreadnorm, writevariants, metadata
    )
  }

  case class DefaultMecabInfo(
    pos: String,
    cat1: String,
    cat2: String,
    form: String,
    dform: String,
    dread: String,
    dreadnorm: String
                               ) extends ReadingAndWriting {
    def reading = KanaUtil.kataToHira(dform)
    def writing = dform
  }

  //動詞,自立,*,*,五段・ラ行,仮定形,すみわたる,スミワタレ,スミワタレ
  def extractDefaultInfo(strings: Array[String]) = {
    val pos = strings(0)
    val cat1 = strings(1)
    val cat2 = strings(4)
    val form = strings(5)
    val dform = strings(6)
    val dread = strings(7)
    val dreadnorm = strings(8)
    DefaultMecabInfo(pos, cat1, cat2, form, dform, dread, dreadnorm)
  }

  def extract(info: String) = {
    val nfo = info.split("\\s*,\\s*")
    nfo.length match {
      case 11 => Some(extractJdicInfo(nfo))
      case 9 => Some(extractDefaultInfo(nfo))
      case _ => None
    }
  }
}

class MalformattedInfoException(info: String) extends RuntimeException(info)
