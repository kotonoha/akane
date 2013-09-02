package ws.kotonoha.akane.config

import org.scalatest.matchers.ShouldMatchers
import org.scalatest.FreeSpec


class ConfigurationSpec extends FreeSpec with ShouldMatchers {
  "config" - {
    "make candidates with hostname" in {
      val name = "addr"
      Configuration.withHostname(name).length should be >= 1
    }

    "make possible names for akane" in {
      val root = "akane"
      Configuration.possibleNamesFor(root).foreach(println)
    }

    "loads config" in {
      val config = Configuration.makeConfigFor("akane")
      config.getString("akane.juman.executable") should equal ("juman")
    }
  }
}