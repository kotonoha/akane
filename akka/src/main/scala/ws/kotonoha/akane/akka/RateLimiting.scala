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

package ws.kotonoha.akane.akka

import akka.actor.{Actor, ActorLogging, ActorRef, Props}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.duration.FiniteDuration

/**
  * @author eiennohito
  * @since 2016/08/22
  */
class MaxAtOnceActor(cfg: RateLimitCfg) extends Actor with ActorLogging {
  import scala.concurrent.duration._
  import MaxAtOnceActor._

  private[this] val queue = new mutable.Queue[QueueInfo]()
  private[this] val inFlight = new ArrayBuffer[InFlightInfo]()
  private val cancel = context.system.scheduler.schedule(1.second, (cfg.lifetime / 3) max 1.second, self, Timeout)(context.dispatcher, self)

  override def postStop(): Unit = {
    cancel.cancel()
  }

  private def cleanFinished(ref: ActorRef, tag: Any) = {
    val present = inFlight.indexWhere(p => p.sender == ref && p.tag == tag, 0)
    if (present == -1) {
      log.warning("could not find inFlight object for {}, {}, have: [{}]", ref, tag, inFlight)
    } else {
      val x = inFlight.remove(present)
      if (cfg.tracing != null) {
        cfg.tracing.finish(x.sender, x.tag, x.start)
      }
    }
  }

  private def enqueue(): Unit = {
    while (inFlight.size < cfg.concurrency && queue.nonEmpty) {
      val obj = queue.dequeue()
      allow(obj.sender, obj.tag)
    }
  }

  private def allow(sndr: ActorRef, tag: Any): Unit = {
    val now = System.currentTimeMillis()
    inFlight += InFlightInfo(sndr, tag, now)
    if (cfg.tracing != null) {
      cfg.tracing.start(sndr, tag, now)
    }
    sndr ! Acknowledge(tag)
  }

  override def receive = {
    case Request(tag) =>
      if (inFlight.length > cfg.concurrency) {
        queue += QueueInfo(sender(), tag)
      } else {
        allow(sender(), tag)
      }
    case Finished(tag) =>
      cleanFinished(sender(), tag)
      enqueue()
    case Timeout =>
      val eol = System.currentTimeMillis() - cfg.lifetime.toMillis
      if (inFlight.exists(_.start < eol)) {
        val collected = inFlight.filter(_.start < eol)
        log.warning(s"{} requests were timed out: [{}]", collected.length, collected)
        for (x <- collected) {
          if (cfg.tracing != null) {
            cfg.tracing.timeout(x.sender, x.tag, x.start)
          }
          inFlight.remove(inFlight.indexOf(x))
        }
      }
      enqueue()
  }
}

object MaxAtOnceActor {
  trait Message
  case class Request(o: Any) extends Message
  case class Acknowledge(o: Any)
  case class Finished(o: Any) extends Message

  private[akka] case object Timeout

  private[akka] case class QueueInfo(sender: ActorRef, tag: Any)
  private[akka] case class InFlightInfo(sender: ActorRef, tag: Any, start: Long)

  def props(cfg: RateLimitCfg): Props = Props(new MaxAtOnceActor(cfg))
}

trait RateLimitTracing {
  def start(ref: ActorRef, tag: Any, time: Long): Unit
  def finish(ref: ActorRef, tag: Any, start: Long): Unit
  def timeout(ref: ActorRef, tag: Any, start: Long): Unit
}

case class RateLimitCfg (
  concurrency: Int,
  lifetime: FiniteDuration,
  tracing: RateLimitTracing = null
)
