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

package ws.kotonoha.akane.analyzers

import java.io.Closeable
import java.util.concurrent.TimeUnit

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.IOUtils

import scala.annotation.tailrec
import scala.concurrent.Future
import scala.util.Try

/**
 * This analyzer implementation uses spawned processes to analyze something.
 * It does its work for 3 times and then gives up.
 * @param spawner
 * @tparam Input
 * @tparam Output
 */
abstract class SpawnedProcessAnalyzer[Input: ToStream, Output: FromStream](spawner: => Process) extends SyncAnalyzer[Input, Output]
  with SubprocessControls with StrictLogging {
  var process: Process = null

  val maxRetries = 3

  override def close() = {
    if (process != null) {
      killProcess()
    }
  }

  private def lazyProcess(): Process = {
    if (process == null) {
      process = spawner
    }
    process
  }

  @tailrec
  private def doWork(retry: Int, input: Input): Try[Output] = {
    if (retry > maxRetries) {
      process = null
      val cname = getClass.getSimpleName
      Future.failed(throw new AnalyzerException(s"$cname have tried for $maxRetries times, but still failed"))
    }

    try {
      val proc = lazyProcess()
      val inp = proc.getOutputStream
      implicitly[ToStream[Input]].writeTo(inp, input)
      inp.flush()
      val os = proc.getInputStream
      val x = implicitly[FromStream[Output]].readFrom(os)
      val es = proc.getErrorStream
      if (es.available() != 0) {
        logger.warn("something was on error stream")
        logger.warn(IOUtils.toString(es))
      }
      x
    } catch {
      case e: Exception =>
        logger.warn(s"failed to parse $input", e)
        process.destroyForcibly()
        process = spawner
        doWork(retry + 1, input)
    }
  }


  override def restart() = {
    if (process != null) {
      killProcess()
    }
    process = spawner
  }

  private def killProcess(): Boolean = {
    logger.debug(s"killing process isalive=${process.isAlive}")
    process.destroyForcibly()
    val ret = process.waitFor(30, TimeUnit.SECONDS)
    process = null
    ret
  }

  def analyzeSync(input: Input): Try[Output] = doWork(0, input)
}

trait SubprocessControls extends Closeable {
  def restart(): Unit
}
