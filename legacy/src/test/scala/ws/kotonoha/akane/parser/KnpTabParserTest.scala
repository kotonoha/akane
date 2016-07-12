package ws.kotonoha.akane.parser

import org.scalatest.{Matchers, FreeSpec}
import org.scalatest.matchers.ShouldMatchers
import scalax.io.{Codec, Resource}

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
      result.bunsetsuCnt shouldBe 5
      result.bunsetsu(4).lexemes.head.reading should be("み")
    }
  }
}


object TreeUtil {
  def classpath(name: String) = {
    val parser = new KnpTabFormatParser
    val lines = Resource.fromClasspath(name).lines()(Codec.UTF8)
    val res = parser.parse(lines)
    res
  }
}
