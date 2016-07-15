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

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FreeSpec
import com.typesafe.config.ConfigFactory


class ConfigurationSpec extends FreeSpec with ShouldMatchers {
  "config" - {
    "make candidates with hostname" in {
      val name = "addr"
      Configuration.withHostname(name).length should be >= 1
    }

    "loads config from file" in {
      val config = Configuration.makeConfigFor("test", ConfigFactory.parseString("user.name = nobody"))
      config.getString("akane.juman.path") should equal ("juman")
    }

    "loads overriden config" in {
      val config = Configuration.makeConfigFor("test", ConfigFactory.parseString("user.name = testuser"))
      config.getString("akane.juman.path") should equal ("other")
    }
  }
}
