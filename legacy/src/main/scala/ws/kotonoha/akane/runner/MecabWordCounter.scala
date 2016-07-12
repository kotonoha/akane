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

package ws.kotonoha.akane.runner

import ws.kotonoha.akane.mecab.{InfoExtractor, MecabParser}
import ws.kotonoha.akane.utils.PathUtil
import java.io.InputStreamReader
import ws.kotonoha.akane.parser.{StreamReaderInput, AozoraParser}
import ws.kotonoha.akane.ast.Sentence
import ws.kotonoha.akane.render.MetaStringRenderer
import collection.mutable
import scalax.file.Path
import scalax.io.Codec
import scalax.file.PathMatcher.GlobNameMatcher

/**
 * @author eiennohito
 * @since 30.10.12 
 */

object MecabWordCounter {

  def main(args: Array[String]) = {
    val prs = new MecabParser
    val files = Path.fromString(args(0)) ** GlobNameMatcher(args(1))
    val encoding = args(2)
    val ignore = PathUtil.stoplist(PathUtil.enumerateStrings(args.toList.drop(3)))
    val map = new mutable.HashMap[String, Int]().withDefault((_) => 0)
    var counter = 0
    for (file <- files;
         inp <- file.inputStream()) {
      val begin = System.nanoTime()
      val sr = new InputStreamReader(inp, encoding)
      val jp = new AozoraParser(new StreamReaderInput(sr))
      try {
        jp.flatMap {
          case Sentence(s) => {
            val msr = new MetaStringRenderer
            val surf = msr.render(s).data
            prs.parse(surf)
          }
          case _ => Nil
        }.foreach(mr => {
          val wr = InfoExtractor.parseInfo(mr)
          val s = wr.safeWriting
          if (!ignore.contains(s)) {
            map(s) += 1
          }
        })
      } catch {
        case e => e.printStackTrace()
      }

      val time = (System.nanoTime() - begin) / 1e9
      println("in %f secs done with %d file, mapsz = %d -- %s".format(time, counter, map.size, file.name))
      counter += 1
    }
    val path = Path.fromString(args(0))
    val out = path.parent.map(_ / "results")
    out foreach (p => {
      val info = map.toList

      val strs = info.sortBy(x => -x._2).map {case (s, cnt) => "%s -> %d".format(s, cnt)}
      p.writeStrings(strs, "\n")(Codec.UTF8)
    })
  }
}
