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

package ws.kotonoha.akane.config

import com.typesafe.config.{Config, ConfigFactory}
import ws.kotonoha.akane.analyzers.juman.JumanConfig

class KnpConfig(val juman: JumanConfig, val executable: String, val params: List[String])
object KnpConfig {
  val knpEx = "akane.knp.executable"
  val knpArgs = "akane.knp.args"

  def apply(config: Config = ConfigFactory.empty()) = {
    import scala.collection.JavaConversions._
    val juman = JumanConfig(config)
    val merged = config.withFallback(AkaneConfig.default)
    val knp = merged.getString(knpEx)
    val args = merged.getStringList(knpArgs).toList
    new KnpConfig(juman, knp, args)
  }
}
