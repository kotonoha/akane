package ws.kotonoha.akane.parser

import org.scalatest.{Matchers, FreeSpec}

/**
 * @author eiennohito
 * @since 15/07/08
 */
class KnpTableTest extends FreeSpec with Matchers {
  "KnpTable" - {
    "calculates correct scope" in {
      val tree = TreeUtil.classpath("trees/weirdKihonku.txt")
      val res = tree.bunsetsuScope(Array(0, 1))
      res shouldEqual Array(0)
    }

    "calculates bunsetsu index for kihonku" in {
      val tree = TreeUtil.classpath("trees/bunsetsu-1.txt")
      tree.bunsetsuIdxForKihonku(6) shouldBe 5
    }
  }
}
