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
  }

  def extract(info: String) = {
    val nfo = info.split("\\s*,\\s*")
    nfo.length match {
      case 11 => extractJdicInfo(nfo)
      case 9 => extractDefaultInfo(nfo)
      case _ => throw new MalformattedInfoException(info)
    }
  }
}

class MalformattedInfoException(info: String) extends RuntimeException(info)
