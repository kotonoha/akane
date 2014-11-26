package ws.kotonoha.akane.parser

import org.scalatest.{Matchers, FreeSpec}
import org.scalatest.matchers.ShouldMatchers
import scalax.io.Resource

/**
 * @author eiennohito
 * @since 2014-05-08
 */
class KnpTabParserTest extends FreeSpec with Matchers {
  "KnpTabParser" - {
    val parser = new KnpTabFormatParser
    "parses a small tree" in {
      val lines = Resource.fromClasspath("knp.tab.txt").lines()
      val result = parser.parse(lines)
      result.bunsetsu should have length(5)
      result.bunsetsu(4).lexemes.head.reading should be("み")
    }
  }
}
