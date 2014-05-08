package ws.kotonoha.akane.parser

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers
import scalax.io.Resource

/**
 * @author eiennohito
 * @since 2014-05-08
 */
class KnpTabParserTest extends FreeSpec with ShouldMatchers {
  "KnpTabParser" - {
    val parser = new KnpTabFormatParser
    "parses a small tree" in {
      val lines = Resource.fromClasspath("knp.tab.txt").lines()
      val result = parser.parse(lines)
      result.bunsetsu should have length(11)
      result.bunsetsu(10).lexemes.head.reading should be("ある")
    }
  }
}
