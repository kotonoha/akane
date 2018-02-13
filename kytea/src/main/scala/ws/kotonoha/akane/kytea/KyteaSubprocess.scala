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

package ws.kotonoha.akane.kytea

import java.io._

import com.typesafe.scalalogging.StrictLogging
import ws.kotonoha.akane.analyzers._
import ws.kotonoha.akane.io.Charsets
import ws.kotonoha.akane.kytea.wire.KyteaSentence

import scala.collection.JavaConverters._
import scala.util.{Failure, Success, Try}

/**
  * @author eiennohito
  * @since 2016/09/06
  */
trait KyteaRaw extends SyncAnalyzer[String, KyteaSentence]
trait KyteaAsync extends AsyncAnalyzer[String, KyteaSentence]

object KyteaSubprocess extends StrictLogging {
  def reader(cfg: KyteaConfig): FromStream[KyteaSentence] = new FromStream[KyteaSentence] {
    private val format = new KyteaFormat(cfg)
    override def readFrom(s: InputStream): Try[KyteaSentence] = {
      val rdr = new BufferedReader(new InputStreamReader(s, Charsets.utf8))
      try {
        Success(format.parse(rdr.readLine()))
      } catch {
        case e: IOException => throw e
        case e: Exception   => Failure(e)
      }
    }
  }

  def writer(): ToStream[String] = new ToStream[String] {
    override def writeTo(s: OutputStream, obj: String): Unit = {
      s.write(obj.getBytes(Charsets.utf8))
      s.write('\n')
      s.flush()
    }
  }

  def process(cfg: KyteaConfig): Process = {
    val cmdline = cfg.cmdline
    logger.debug(s"starting kytea from: ${cfg.executable}, model: ${cfg.model}")
    val pb = new ProcessBuilder(cmdline.asJava)
    val proc = pb.start()
    logger.debug(s"started kytea: ${proc.isAlive}")
    proc
  }

  private final class Impl(cfg: KyteaConfig)
      extends SpawnedProcessAnalyzer[String, KyteaSentence](process(cfg))(writer(), reader(cfg))
      with KyteaRaw

  def create(cfg: KyteaConfig): KyteaRaw with SubprocessControls = new Impl(cfg)
}
