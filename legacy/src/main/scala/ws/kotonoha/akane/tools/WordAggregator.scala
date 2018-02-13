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

package ws.kotonoha.akane.tools

import java.io.InputStreamReader
import java.nio.file.{Path, Paths}

import com.typesafe.scalalogging.StrictLogging
import ws.kotonoha.akane.ast.Sentence
import ws.kotonoha.akane.juman.{JumanDaihyou, JumanPipeExecutor}
import ws.kotonoha.akane.parser.{AozoraParser, StreamReaderInput}
import ws.kotonoha.akane.render.MetaStringRenderer

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{Await, ExecutionContext, Future}

/**
  * @author eiennohito
  * @since 27.03.13
  */
case class Information(writing: String, reading: String, pos: String)

class LazyTheadLocal[T <: AnyRef](factory: => T) {
  private val value = new ThreadLocal[T]

  def get() = {
    if (value.get() == null) {
      value.set(factory)
    }
    value.get()
  }

  def clear() = value.remove()
}

object WordAggregator extends StrictLogging {
  import ws.kotonoha.akane.resources.FSPaths._
  implicit val context = {
    val ex = new ForkJoinPool(8)
    ExecutionContext.fromExecutor(ex)
  }

  object juman extends LazyTheadLocal(makeJuman)

  def makeJuman: JumanPipeExecutor = {
    JumanPipeExecutor.apply()
  }

  def loadNs(path: Path, level: String) = {
    path
      .lines()
      .grouped(3)
      .map {
        case Seq(w, r, _) =>
          if (w == "") JumanDaihyou(r, "") -> level
          else JumanDaihyou(w, r) -> level
      }
      .toMap
  }

  val nmap = {
    val base = Paths.get("e:\\Temp\\wap_soft\\jlpt\\")
    val seq = Seq.range(5, 0, -1).map(i => loadNs(base / s"n$i.txt", s"N$i"))
    seq.reduce(_ ++ _)
  }

  val ignorePos = Set("特殊", "助詞", "判定詞", "接尾辞", "指示詞", "助動詞", "接頭辞", "接続詞")

  def parse(in: InputStreamReader) = {
    val sri = new StreamReaderInput(in)
    val ap = new AozoraParser(sri)
    val juman = this.juman.get()
    val rend = new MetaStringRenderer
    var words = 0
    val res = ap.foldLeft(Map[Information, Int]().withDefaultValue(0)) {
      case (m, Sentence(node)) =>
        val sent = rend.render(node)
        val entries = juman.parse(sent.data).filterNot(e => ignorePos.contains(e.spPart))
        entries.foldLeft(m) {
          case (m, je) =>
            val d = je.daihyou
            words += 1
            val i = Information(d.writing, d.reading, je.spPart)
            m.updated(i, m(i) + 1)
        }
      case (m, _) => m
    }
    (res, words)
  }

  def main(args: Array[String]) {
    val path = Paths.get(args(0))
    val enc = args(1)
    logger.info(s"processing directory $path with encoding $enc")

    val ents = path.children().map { f =>
      Future.apply {
        val res = for {
          is <- f.inputStream
        } yield {
          val rd = new InputStreamReader(is, enc)
          val (map, words) = parse(rd)
          logger.info(s"Parsed file: ${f.name} -- $words words")
          map
        }
        Some(res.obj)
      }
    }

    import scala.concurrent.duration._

    val extracted = Future.sequence(ents.toSeq).map(x => x.flatten)
    val res = Await.result(extracted, 1.hour)

    val data = res.reduce { (m1, m2) =>
      m2.foldLeft(m1) {
        case (m, (k, cnt)) => m.updated(k, m(k) + cnt)
      }
    }

    val sorted = data.toSeq.sortBy(-_._2)

    val tout = sorted.map {
      case (nfo, cnt) =>
        val jd = JumanDaihyou(nfo.writing, nfo.reading)
        val item = nmap.getOrElse(jd, "")
        Seq(nfo.pos, nfo.writing, nfo.reading, item, cnt)
          .foldLeft(new StringBuilder(512)) { (sb, i) =>
            sb.append("\"")
            sb.append(i)
            sb.append("\"\t")
          }
          .result()
    }

    val outf = Paths.get("e:/Temp/word_data.csv")
    outf.writeStrings(tout, "\n")
  }
}
