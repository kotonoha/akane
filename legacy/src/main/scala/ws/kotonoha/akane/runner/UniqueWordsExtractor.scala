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

import ws.kotonoha.akane.pipe.juman.{JumanQuery, ParsedQuery}

import scalax.file.Path
import ws.kotonoha.akane.parser.{AozoraParser, StreamReaderInput}
import java.io.{PrintWriter, InputStreamReader}
import akka.actor.{Props, ActorSystem, Actor}
import ws.kotonoha.akane.juman.{JumanDaihyou, JumanPipeExecutor}
import ws.kotonoha.akane.statistics.{UniqueWordsExtractor => WE}
import ws.kotonoha.akane.utils.PathUtil
import concurrent.{ExecutionContext, Await}
import concurrent.duration._

/**
 * @author eiennohito
 * @since 07.09.12
 */

case class JumanQuary(q: String)

class SmallJumanActor extends Actor {
  val je = JumanPipeExecutor.apply()
  def receive = {
    case JumanQuery(q) => sender ! ParsedQuery(je.parse(q))
  }
}

object UniqueWordsExtractor {
  import ws.kotonoha.akane.unicode.KanaUtil.{kataToHira => hira}
  def main(args: Array[String]) {
    val as = ActorSystem("uwe")
    implicit val ec: ExecutionContext = as.dispatcher
    val j = as.actorOf(Props[SmallJumanActor])
    val fn = Path.fromString(args(0))
    val enc = args(1)
    //PathMatcher.GlobPathMatcher()
    val paths = PathUtil.enumerateStrings(args.toList.drop(2))
    val ignore = PathUtil.stoplist(paths)
    for (is <- fn.inputStream) {
      val pw = new PrintWriter(args(0) + ".out", "utf-8")
      val sr = new InputStreamReader(is, enc)
      val inp = new StreamReaderInput(sr)
      val parser = new AozoraParser(inp) toArray
      val f = new WE(j, as.dispatcher)
      val fut = f.uniqueWords(parser, ignore)
      val a = fut map (x => {
        x.foreach {
          case JumanDaihyou(s, "") => pw.println(s)
          case JumanDaihyou(s, r) if hira(s).equals(r) => pw.println(s)
          case JumanDaihyou(s, r) => pw.printf("%s|%s\n", s, r)
        }
        Nil
      })
      Await.ready(a, 1 day)
      pw.close()
    }
    as.shutdown()
    as.awaitTermination()
  }
}