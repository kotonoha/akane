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

package ws.kotonoha.akane.statistics

import akka.actor.ActorRef
import akka.pattern.ask
import ws.kotonoha.akane.{ParsedQuery, JumanQuery}
import ws.kotonoha.akane.ast.{Sentence, HighLvlNode}
import akka.dispatch.{ExecutionContext, Future}
import ws.kotonoha.akane.render.MetaStringRenderer
import ws.kotonoha.akane.juman.{JumanDaihyou, JumanUtil}
import akka.util.Timeout

/**
 * @author eiennohito
 * @since 20.08.12
 */

class UniqueWordsExtractor(juman: ActorRef, ex: ExecutionContext) {
  import akka.util.duration._
  implicit val timeout = Timeout(5 seconds)
  implicit val ec : ExecutionContext = ex

  private def parse(q: String) = (juman ? JumanQuery(q)).mapTo[ParsedQuery]

  def uniqueWords(nodes: TraversableOnce[HighLvlNode], stoplist: Set[String] = Set()): Future[List[JumanDaihyou]] = {
    val mir = new MetaStringRenderer()
    val items = nodes flatMap {
      case Sentence(s) => List(mir.render(s).data)
      case _ => Nil
    } toList
    val f = items map { s => parse(s) }
    Future.sequence(f) map {
      i => i.flatMap {
        pq => pq.inner.map(JumanUtil.daihyouWriting(_)) filter {
          j => JumanUtil.ignored(j.writing)
        } filterNot {
          j => stoplist.contains(j.writing) || stoplist.contains(j.reading)
        }
      } distinct
    }
  }

}
