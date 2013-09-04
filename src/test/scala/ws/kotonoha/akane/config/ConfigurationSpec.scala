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