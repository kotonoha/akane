package ws.kotonoha.akane.utils.diff

import org.scalatest.{FreeSpec, Matchers}

/**
  * @author eiennohito
  * @since 2015/10/13
  */
class ClampedLevensteinSpec extends FreeSpec with Matchers {
  "levenstein calculator" - {
    def calc(sz: Int = 4) = new ClapmedLevenstein(sz)

    "hat -> cat = 1" in {
      calc().distance("hat", "cat") shouldBe 1
    }

    "apple -> ringo >= 4" in {
      calc().distance("apple", "ringo") shouldBe 4
    }

    "dabce -> dance = 1" in {
      calc(3).distance("dance", "dabce") shouldBe 1
    }

    "monster -> hipster = 3" in {
      calc().distance("monster", "hipster") shouldBe 3
    }

    "whatever -> goat >= 3" in {
      calc(3).distance("whatever", "goat") should be >= 3
    }

    "apport -> aport = 1" in {
      calc(2).distance("apport", "aport") shouldBe 1
    }

    "a -> what = 3" in {
      calc(5).distance("a", "what") shouldBe 3
      calc(4).distance("a", "what") shouldBe 3
      calc(3).distance("a", "what") shouldBe 3
    }
  }

  "levenstein calculator with costs" - {
    def calc(sz: Int, eql: CharEquality) = new ClapmedLevenstein(sz, eql)

    "deletions and insertions are costly" - {
      val eqv = new SimpleCharEquality {
        override def insertionCost(ch: Int) = 5
      }

      "monster -> hipster = 3" in {
        calc(3, eqv).distance("monster", "hipster") shouldBe 3
      }

      "apport -> aport >= 3" in {
        calc(5, eqv).distance("apport", "aport") should be >= 5
      }
    }

    "charset equality" - {
      val eqv = new CharsetEquality(".,?!。！？＋＞  ", 1, 10, 1, 10)

      "test -> test. = 1" in {
        calc(3, eqv).distance("test", "test.") shouldBe 1
      }

      ".test. -> test.. = 2" in {
        calc(3, eqv).distance(".test.", "test..") shouldBe 2
      }
    }
  }
}
