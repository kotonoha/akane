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
import scalax.io.Codec
import ws.kotonoha.akane.utils.CalculatingIterator
import ws.kotonoha.akane.unicode.UnicodeUtil
import collection.mutable.ListBuffer
import collection.immutable

/**
 * @author eiennohito
 * @since 14.11.12 
 */

class StaticGrouper(in: Iterator[String]) extends CalculatingIterator[String] {
  lazy val iter = in.map(x => re.pattern.split(x, -1)).buffered
  lazy val re = ",".r

  lazy val queue = new scala.collection.mutable.Queue[String]()

  lazy val kanaRE = "[\\u3040-\\u309f\\u30a0-\\u30ff]+".r

  def forms(item: Array[String], forms: List[String]): List[String] = {
    val frm = item(10)
    val items = if (kanaRE.pattern.matcher(frm).find()) {
      val rawre = kanaRE.replaceAllIn(frm, ".*")
      val expr = "^%s$".format(rawre).r
      forms.filter(expr.pattern.matcher(_).matches())
    } else frm :: Nil

    val self =  if (UnicodeUtil.hasKanji(frm)) frm :: Nil else Nil
    (items.flatMap(JMDictGrouper.items.get(_).flatten) ++ items ++ self).distinct
  }

  def fillQueue(): Option[String] = {
    if (!iter.hasNext)
      return None
    val rd = iter.head(11)
    val lb = new ListBuffer[Array[String]]()
    while (iter.hasNext && iter.head(11).equals(rd)) {
      lb += iter.next()
    }
    val items = lb.toList
    val dforms = items.map(_(10)).filter{x => UnicodeUtil.hasKanji(x)} //drop nonkanji writings
    val x :: xs = items.map { i => i.mkString(",") + ",%s".format(forms(i, dforms).mkString("/"))}
    queue ++= xs
    Some(x)
  }

  protected def calculate() = queue.dequeueFirst(_ => true) orElse(fillQueue())
}

object JMDictGrouper {
  val path = Path.fromString("e:\\Temp\\wap_soft\\JMdict.writ")

  lazy val items = {
    val lines = path.lines().map(_.split(";"))
    lines.flatMap(x => x.map(_ -> x)).toMap
  }
}

object MecabStaticGrouper {
  implicit val codec = Codec.UTF8
  def main(args: Array[String]) = {
    val p = Path.fromString(args(0))
    val o = Path.fromString(args(0) + ".out")
    val lines = p.lines()
    val sg = new StaticGrouper(lines.toIterator)
    o.writeStrings(sg.toTraversable, "\n")
  }
}
