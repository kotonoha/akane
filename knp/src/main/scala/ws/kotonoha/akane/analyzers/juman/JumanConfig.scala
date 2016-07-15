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

package ws.kotonoha.akane.analyzers.juman

import com.typesafe.config.{Config, ConfigFactory}
import ws.kotonoha.akane.config.AkaneConfig

/**
  * @author eiennohito
  * @since 2016/07/12
  */
class JumanConfig(val executable: String, val encoding: String, val params: Seq[String])

object JumanConfig {

  import scala.collection.JavaConverters._

  val jumanEx = "akane.juman.executable"
  val jumanArgs = "akane.juman.args"
  val jumanEncoding = "akane.juman.encoding"

  def apply(config: Config = ConfigFactory.empty()) = {
    val merged = config.withFallback(AkaneConfig.default)
    val exec = merged.getString(jumanEx)
    val enc = merged.getString(jumanEncoding)
    val args = merged.getStringList(jumanArgs).asScala
    new JumanConfig(exec, enc, args)
  }
}
