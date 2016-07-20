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

package ws.kotonoha.akane.analyzers.jumanpp

import java.io.BufferedReader

import org.apache.commons.lang3.StringUtils
import ws.eiennohito.utils.Foreach
import ws.kotonoha.akane.analyzers.juman.{JumanOption, JumanPos}
import ws.kotonoha.akane.analyzers.jumanpp.wire.{Lattice, LatticeNode}

import scala.collection.mutable.ArrayBuffer

/**
  * @author eiennohito
  * @since 2016/07/15
  */
class JppLatticeParser {

  def intList(s: String, sep: Char): Seq[Int] = {
    val splt = StringUtils.split(s, sep)
    splt.map(_.toInt)
  }

  def parseLine(line: String): LatticeNode = {
    val splitted = StringUtils.splitPreserveAllTokens(line, '\t')
    val nodeId = splitted(1).toInt
    val prev = intList(splitted(2), ';')
    val tokStart = splitted(3).toInt
    val tokEnd = splitted(4).toInt
    val surfForm = splitted(5)
    val canon = splitted(6)
    val reading = splitted(7)
    val midasi = splitted(8)
    val pos1 = splitted(10)
    val pos2 = splitted(12)
    val pos3 = splitted(14)
    val pos4 = splitted(16)
    val features = splitted(17)
    val allFeatures = StringUtils.split(features, '|')

    var fscore = Float.NaN
    var lmscore = Float.NaN
    var anscore = Float.NaN
    var rank: Seq[Int] = Nil
    var rest = new ArrayBuffer[JumanOption]()

    Foreach.fori(0, allFeatures.length) { i =>
      val f = allFeatures(i)
      val semi = StringUtils.indexOf(f, ':')
      if (semi == -1) {
        rest += JumanOption(f, None)
      } else {
        val key = f.substring(0, semi)
        val other = f.substring(semi + 1)
        key match {
          case "特徴量スコア" => fscore = other.toFloat
          case "言語モデルスコア" => lmscore = other.toFloat
          case "形態素解析スコア" => anscore = other.toFloat
          case "ランク" => rank = intList(other, ';')
          case _ => rest += JumanOption(key, Some(other))
        }
      }
    }

    LatticeNode(
      nodeId,
      prev,
      tokStart,
      tokEnd,
      surfForm, canon, reading, midasi,
      JumanPos(pos1.toInt, pos2.toInt, pos3.toInt, pos4.toInt),
      fscore,
      lmscore,
      anscore,
      rank,
      rest
    )
  }

  def parse(in: BufferedReader): Lattice = {
    val nodes = new ArrayBuffer[LatticeNode]()

    var line = ""
    var comment: Option[String] = None
    while ({
      line = in.readLine()
      line != null && line != "EOS"
    }) {
      if (line.startsWith("#")) {
        comment = Some(line)
      } else {
        nodes += parseLine(line)
      }
    }

    Lattice(comment, nodes)
  }
}