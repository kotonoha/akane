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

package ws.kotonoha.akane.misc.tatoeba

import org.apache.commons.lang3.StringUtils
import ws.kotonoha.akane.utils.ParseUtil

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

/**
  * @author eiennohito
  * @since 2016/09/12
  */
case class BLineUnit(headword: String, reading: Option[String], surface: Option[String], sense: Int, recommended: Boolean) {
  def original: String = {
    if (surface.isDefined) surface.get else headword
  }
}


case class BLine(japId: Long, engId: Long, content: Seq[BLineUnit])

object BLinesReader {

  //其の[01]{その} 家(いえ)[01] は 可也{かなり} ぼろ屋[01]~ になる[01]{になっている}
  //The fields after the indexing headword ()[]{}~ must be in that order.
  def readUnit(data: CharSequence): BLineUnit = {
    var hwEnd = data.length()
    val rdStart = StringUtils.indexOf(data, '(')
    var rdEnd = 0
    if (rdStart > 0) {
      if (rdStart < hwEnd) hwEnd = rdStart
      rdEnd = StringUtils.indexOf(data, ')', rdStart)
      if (rdEnd < 0) {
        throw new RuntimeException(s"could not parse $data: there was no ) after (")
      }
    }

    val senseStart = StringUtils.indexOf(data, '[', rdEnd)
    var senseEnd = rdEnd
    var sense = 0
    if (senseStart > 0) {
      if (senseStart < hwEnd) hwEnd = senseStart
      senseEnd = StringUtils.indexOf(data, ']', senseStart)
      if (senseEnd < 0) {
        throw new RuntimeException(s"could not parse $data: there was no ] after [")
      }
      sense = ParseUtil.parseInt(data, senseStart + 1, senseEnd)
    }

    val surfStart = StringUtils.indexOf(data, '{', senseEnd)
    var surfEnd = senseEnd
    if (surfStart > 0) {
      if (surfStart < hwEnd) hwEnd = surfStart
      surfEnd = StringUtils.indexOf(data, '}', surfStart)
      if (surfEnd < 0) {
        throw new RuntimeException(s"could not parse $data: there was no } after {")
      }
    }

    val recomIdx = StringUtils.indexOf(data, '~', senseEnd)
    var recommended = false
    if (recomIdx > 0) {
      if (recomIdx < hwEnd) hwEnd = recomIdx
      recommended = true
    }

    val reading = if (rdStart < 0) None else {
      val rd = data.subSequence(rdStart + 1, rdEnd).toString
      Some(rd)
    }

    val surface = if (surfStart < 0) None else {
      val sf = data.subSequence(surfStart + 1, surfEnd).toString
      Some(sf)
    }

    BLineUnit(
      headword = data.subSequence(0, hwEnd).toString,
      reading = reading,
      surface = surface,
      sense = sense,
      recommended = recommended
    )
  }

  def parseUnits(data: CharSequence, from: Int, to: Int): Seq[BLineUnit] = {
    val units = new ArrayBuffer[BLineUnit]()
    parseUnitsImpl(units, data, from, to)
    units
  }

  @tailrec
  private def parseUnitsImpl(units: ArrayBuffer[BLineUnit], data: CharSequence, from: Int, to: Int): Unit = {
    val spaceIdx = StringUtils.indexOf(data, ' ', from)
    if (spaceIdx < 0 || spaceIdx > to) {
      units += readUnit(data.subSequence(from, to))
    } else {
      units += readUnit(data.subSequence(from, spaceIdx))
      parseUnitsImpl(units, data, spaceIdx + 1, to)
    }
  }
}
