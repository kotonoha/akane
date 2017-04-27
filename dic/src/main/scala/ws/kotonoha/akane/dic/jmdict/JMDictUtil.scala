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

package ws.kotonoha.akane.dic.jmdict

import java.io.InputStream
import java.nio.file.Path
import java.util.zip.GZIPInputStream

import ws.kotonoha.akane.dic.jmdict
import ws.kotonoha.akane.io.LineIterator
import ws.kotonoha.akane.resources.FSPaths


/**
  * @author eiennohito
  * @since 2016/07/27
  */
object JMDictUtil {

  private val jmdictVerRe = """<!-- JMdict created: (\d{4}-\d{2}-\d{2}) -->""".r

  def extractVersion(is: InputStream): Option[String] = {
    val lines = LineIterator(is)
    lines.take(1000).collectFirst {
      case jmdictVerRe(date) => date
    }
  }

  def extractVersion(file: Path): Option[String] = {
    import FSPaths._
    file.inputStream.map(i => extractVersion(convertStream(file.extension, i))).obj
  }

  def convertStream(ext: String, is: InputStream): InputStream = {
    ext match {
      case "gz" => new GZIPInputStream(is)
      case _ => is
    }
  }


  def calculatePriority(entry: CommonInfo): Int = {
    val prs = entry.priority.map {
      case jmdict.Priority.news1 => 2
      case jmdict.Priority.news2 => 1
      case jmdict.Priority.ichi1 => 2
      case jmdict.Priority.ichi2 => 1
      case jmdict.Priority.spec1 => 2
      case jmdict.Priority.spec2 => 1
      case jmdict.Priority.gai1 => 2
      case jmdict.Priority.gai2 => 1
      case _ => 0
    }

    val avg = prs.sum.toFloat / prs.length
    Math.round(avg) max entry.freq.map(_ / 24).getOrElse(0)
  }

  def calculatePriority(s: Seq[CommonInfo]): Int = {
    if (s.isEmpty) 0 else s.view.map(calculatePriority).max
  }

  def calculatePriority(entry: JmdictEntry): Int = {
    calculatePriority(entry.readings) max calculatePriority(entry.writings)
  }

  object priorityOrder extends Ordering[JmdictEntry] {
    override def compare(x: JmdictEntry, y: JmdictEntry) = {
      val xp = calculatePriority(x.readings) max calculatePriority(x.writings)
      val yp = calculatePriority(y.readings) max calculatePriority(y.writings)
      xp.compareTo(yp)
    }
  }

  def cleanMeanings(mns: Seq[MeaningInfo], langs: Set[String]): Seq[MeaningInfo] = {
    mns.map { mi =>
      val content = mi.content.filter(l => langs.contains(l.lang))
      mi.copy(content = content)
    }
  }

  def cleanLanguages(e: JmdictEntry, langs: Set[String]): JmdictEntry = {
    val ms = cleanMeanings(e.meanings, langs)
    e.copy(meanings = ms)
  }

  def cleanLanguages(es: Seq[JmdictEntry], langs: Set[String]): Seq[JmdictEntry] = es.map(cleanLanguages(_, langs))

  val verbTags = Set(
    JmdictTag.v1,
    JmdictTag.v2aS,
    JmdictTag.v4h,
    JmdictTag.v4r,
    JmdictTag.v5,
    JmdictTag.v5aru,
    JmdictTag.v5b,
    JmdictTag.v5g,
    JmdictTag.v5k,
    JmdictTag.v5kS,
    JmdictTag.v5m,
    JmdictTag.v5n,
    JmdictTag.v5r,
    JmdictTag.v5rI,
    JmdictTag.v5s,
    JmdictTag.v5t,
    JmdictTag.v5u,
    JmdictTag.v5uS,
    JmdictTag.v5uru,
    JmdictTag.v5z,
    JmdictTag.v5z,
    JmdictTag.vk,
    JmdictTag.vn,
    JmdictTag.vr,
    JmdictTag.vs,
    JmdictTag.vsS,
    JmdictTag.vsI
  )

  val adjTags = Set(
    JmdictTag.adj,
    JmdictTag.adjI,
    JmdictTag.adjNa,
    JmdictTag.adjNo,
    JmdictTag.adjF,
    JmdictTag.adjT,
    JmdictTag.adjPn
  )

  val advTags = Set(
    JmdictTag.adv,
    JmdictTag.advTo
  )
}
