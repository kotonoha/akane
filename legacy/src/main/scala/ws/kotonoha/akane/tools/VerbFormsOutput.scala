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

package ws.kotonoha.akane.tools

import scalax.file.Path
import util.Random
import ws.kotonoha.akane.conjuation.{Renderable, Verb}
import org.apache.commons.lang3.StringUtils

/**
  * @author eiennohito
  * @since 16.11.12
  */
object VerbFormsOutput {
  val tags = {
    val lines = Path.fromString("e:\\Temp\\wap_soft\\verbs.txt").lines()
    val flt = lines.map(_.split("\t")).flatMap {
      case Array(s1, s2) => {
        Some(StringUtils.strip(s1, "\"") -> StringUtils.strip(s2, "\""))
      }
      case _ => None
    }
    flt.toMap
  }

  def main(args: Array[String]) {
    val items = Path.fromString("e:\\Temp\\wap_soft\\lists\\asd.out").lines().take(250).toList

    val sfled = Random.shuffle(items)

    val candidates = sfled.toStream.flatMap(i => {
      val tg = tags.get(i)
      tg.map(t => Verb.fromJMDict(i, t))
    })

    val renderers: Array[(Int, Verb => Renderable)] = Array(
      (0, x => x.meirei),
      (1, x => x.possible.past),
      (2, x => x.teForm.iru),
      (3, x => x.sieki.masuStem.masu),
      (4, x => x.ukemi.meirei),
      (5, x => x.naiStem.nai),
      (6, x => x.siekiUkemi.teForm.iru.past),
      (7, x => x.ukemi),
      (8, x => x.siekiUkemi),
      (9, x => x.possible.taStem.ta),
      (10, x => x.possible.taStem),
      (11, x => x.past),
      (12, x => x.masuStem.masu.taStem.ta),
      (13, x => x.possible.kateiStem.ba),
      (13, x => x.sieki.kateiStem.ba),
      (13, x => x.kateiStem.ba),
      (13, x => x.ukemi.kateiStem.ba)
    )

    def render(pos: Int, dic: Option[String], item: Option[String]) = {
      item.zip(dic).map {
        case (i, i2) => {
          val sb = new StringBuilder
          for (p <- 0.until(renderers.length)) {
            if (p == pos) {
              sb.append(i2)
              sb.append("->")
              sb.append(i)
            }
            //sb.append('\t')
          }
          sb.toString()
        }
      }
    }

    val x = 1.to(60).map(x => Random.nextInt(renderers.length))

    val res = candidates
      .zip(x)
      .flatMap {
        case (w, ind) => {
          val r = renderers(ind)
          render(r._1, w.render, r._2(w).render)
        }
      }
      .toList

    res.foreach(println(_))
  }
}
