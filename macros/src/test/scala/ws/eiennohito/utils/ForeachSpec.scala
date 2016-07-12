package ws.eiennohito.utils

import org.scalatest.{FreeSpec, Matchers}

/**
 * @author eiennohito
 * @since 15/08/14
 */
class ForeachSpec extends FreeSpec with Matchers {
  "Foreach" - {
    "works in simple case" in {
      var smt = 0
      Foreach.fori(3, 5) { i => smt += i }
      smt shouldBe 7
    }

    "works even nested" in {
      var smt = 0
      Foreach.fori(1, 3) { i =>
        Foreach.fori(1, 3) { j =>
          smt += i * j
        }
      }

      smt should be (1 + 2 + 2 + 4)
    }
  }
}
