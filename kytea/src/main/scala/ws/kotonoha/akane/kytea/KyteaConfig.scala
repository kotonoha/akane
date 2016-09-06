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

import com.typesafe.config.Config

import scala.collection.mutable.ArrayBuffer

/**
  * @author eiennohito
  * @since 2016/09/06
  */

case class KyteaConfig(model: Option[String]) {
  def cmdline: Seq[String] = {
    val result = new ArrayBuffer[String]()
    model.foreach { mf =>
      result.append("-model", mf)
    }

    result.append("-wordbound", KyteaConfig.wordBound)
    result.append("-tagbound", KyteaConfig.tagBound)
    result.append("-elembound", KyteaConfig.elemBound)

    result
  }
}

object KyteaConfig {
  import ws.kotonoha.akane.config.ScalaConfig._
  def apply(cfg: Config): KyteaConfig = {
    val subconf = cfg.getConfig("akane.kytea")

    val model = cfg.optStr("model")
    KyteaConfig(model)
  }

  val wordBound = "\u0001"
  val tagBound = "\u0002"
  val elemBound = "\u0003"
}