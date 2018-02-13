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

import ws.kotonoha.akane.parser.{AozoraParser, StreamReaderInput}
import scalax.file.Path
import java.io.{InputStreamReader, PrintWriter}
import ws.kotonoha.akane.transform.JumanTransformer
import ws.kotonoha.akane.juman.JumanPipeExecutor
import ws.kotonoha.akane.ast.Sentence
import ws.kotonoha.akane.render.HtmlRenderer
import xml.{MinimizeMode, Utility}

/**
  * @author eiennohito
  * @since 17.08.12
  */
object HtmlJumanRenderer {
  def main(args: Array[String]) = {
    val fn = args(0)
    val res = Path.fromString(fn).inputStream
    val pe = JumanPipeExecutor.apply()
    val jt = new JumanTransformer(pe)
    for (is <- res) {
      val rdr = new InputStreamReader(is, args(1))
      val inp = new StreamReaderInput(rdr)
      val parser = new AozoraParser(inp)
      val hr = new HtmlRenderer
      val sb = new StringBuilder(8 * 1024)
      val parsed = parser.toArray
      val nodes = parsed
        .map {
          case s: Sentence => jt.transformSentence(s)
          case n           => n
        }
        .map {
          hr.render(_)
        }
      val out = Path.fromString(args(0) + ".html")

      val doc =
        <html>
          <head>
          </head>
          <body>
            {nodes.toList}
          </body>
        </html>

      val pw = new PrintWriter(out.path, "utf-8")

      sb.clear()
      Utility.sequenceToXML(doc, sb = sb, minimizeTags = MinimizeMode.Always)
      println(sb.toString())
      pw.println(sb.toString())

      pw.close()

    }

  }
}
