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

package ws.kotonoha.akane.kanji.kradfile

import java.net.URL
import scalax.io.Codec

/**
  * @author eiennohito
  * @since 08.07.13
  */
object RadicalDb {

  def loadRadfile(url: URL) = {
    import scalax.io.JavaConverters._

    val input = url.asInput
    val lines = input.lines()(Codec.UTF8).filterNot(x => x.startsWith("#")).drop(1)
    lines.map { l =>
      val pos = l.indexOf(":")
      val kanji = l.substring(0, pos).trim
      val rest = l.substring(pos + 1).split(" ").toSeq.map(_.trim).filter(x => x.length > 0)
      kanji -> rest
    } toMap
  }

  /**
    * Kanji -> List of radicals mapping
    *
    * It is loaded from kradfile(s)
    */
  lazy val table = {
    val kradfile = RadicalDb.getClass.getClassLoader.getResource("kradfile/kradfile")
    val kradfile2 = RadicalDb.getClass.getClassLoader.getResource("kradfile/kradfile2")
    loadRadfile(kradfile) ++ loadRadfile(kradfile2)
  }

  /**
    * A reverse Radical -> Kanji mapping
    *
    * 2 foldlefts and no mutable state.
    */
  lazy val reverse = {
    table.foldLeft(Map[String, Seq[String]]()) {
      case (m, (k, seq)) =>
        seq.foldLeft(m) {
          case (m, rad) =>
            val cur = m.get(rad)
            val upd = cur match {
              case Some(x) => Seq(k) ++ x
              case _       => Seq(k)
            }
            m.updated(rad, upd)
        }
    }
  }
}
