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

package ws.kotonoha.akane.runner

import scalax.file.Path
import java.io.{PrintWriter, InputStreamReader}
import ws.kotonoha.akane.parser.{DebugInput, AozoraParser, StreamReaderInput}
import ws.kotonoha.akane.juman.JumanPipeExecutor
import ws.kotonoha.akane.statistics.FrequencyAnalyzer
import scalax.io.Codec
import collection.mutable

/**
 * @author eiennohito
 * @since 18.08.12
 */

object JumanFrequencyAnalyzer {
  def main(args: Array[String]) = {
    val path = Path(args(0))
    val pex = JumanPipeExecutor.apply()
    val enc = args(1)
    val ignore = args.slice(2, args.length).foldLeft (new mutable.HashSet[String]()) {case (hs, path) => {
      val p = Path(path)
      p.lines()(Codec.UTF8).filter(!_.startsWith("#")).foreach { hs += _ }
      hs
    }}.toSet[String]
    for (is <- path.inputStream) {
      val isr = new InputStreamReader(is, enc)
      val inp = new StreamReaderInput(isr)
      val jp = new AozoraParser(inp)
      val fa = new FrequencyAnalyzer(pex, ignore)
      val info = fa.analyze(jp)
      val pw = new PrintWriter(path.path + ".freq", "utf-8")
      pw.println("total: %d results".format(info.cnt))
      info.items.foreach{ i => pw.println("%s# -> %d".format(i.item, i.cnt)) }
      pw.close()
    }
  }
}
