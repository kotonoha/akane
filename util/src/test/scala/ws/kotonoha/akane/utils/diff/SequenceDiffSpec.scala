package ws.kotonoha.akane.utils.diff

import org.scalatest.{FreeSpec, Matchers}

/**
  * @author eiennohito
  * @since 2016/09/12
  */
//noinspection ZeroIndexToHead
class SequenceDiffSpec extends FreeSpec with Matchers {
  "SequenceDiff" - {
    "works with a single equal letter" in {
      val diff = new TextDiffCalculator().calculate("a", "a")
      diff.score shouldBe 0
      diff.spans should have length 1
      diff.spans.head.kind shouldBe DiffTypes.TRANSITION_EQUALS
    }

    "works with two same letters" in {
      val diff = new TextDiffCalculator().calculate("aa", "aa")
      diff.score shouldBe 0
      diff.spans should have length 1
      diff.spans.head shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 0, 2, 0, 2)
    }

    "works with two equal letters" in {
      val diff = new TextDiffCalculator().calculate("ab", "ab")
      diff.score shouldBe 0
      diff.spans should have length 1
      diff.spans.head shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 0, 2, 0, 2)
    }

    "works with one and two letters" in {
      val diff = new TextDiffCalculator().calculate("a", "ab")
      diff.score shouldBe 1
      diff.spans should have length 2
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 0, 1, 0, 1)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_INSERT, 1, 1, 1, 2)
    }

    "works with two and one letters" in {
      val diff = new TextDiffCalculator().calculate("ab", "a")
      diff.score shouldBe 1
      diff.spans should have length 2
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 0, 1, 0, 1)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_DELETE, 1, 2, 1, 1)
    }

    "works with two and three letters" in {
      val diff = new TextDiffCalculator().calculate("aba", "aa")
      diff.score shouldBe 1
      diff.spans should have length 3
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 0, 1, 0, 1)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_DELETE, 1, 2, 1, 1)
      diff.spans(2) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 2, 3, 1, 2)
    }

    "works with simple sequences" in {
      val s1 = "world!"
      val s2 = "weqld!"
      val diff = new TextDiffCalculator().calculate(s1, s2)
      diff.score shouldBe 2
      diff.spans should have length 3
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 0, 1, 0, 1)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 1, 3, 1, 3)
      diff.spans(2) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 3, 6, 3, 6)
    }

    "works with wtf" in {
      val diff = new TextDiffCalculator().calculate("hello, derp!", "goodbye, dwtferp!")
      diff.score shouldBe 10
      diff.spans should have length 4
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 0, 5, 0, 7)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 5, 8, 7, 10)
      diff.spans(2) shouldBe DiffSpan(DiffTypes.TRANSITION_INSERT, 8, 8, 10, 13)
      diff.spans(3) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 8, 12, 13, 17)
    }

    "works with reading and writing" in {
      val diff = new TextDiffCalculator().calculate("付き合います", "つきあいます")
      diff.score shouldBe 2
      diff.spans should have length 4
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 0, 1, 0, 1)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 1, 2, 1, 2)
      diff.spans(2) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 2, 3, 2, 3)
      diff.spans(3) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 3, 6, 3, 6)
    }

    "works with reading and writing of different length" in {
      val diff = new TextDiffCalculator().calculate("潰れ混んだ", "つぶれこんだ")
      diff.score shouldBe 3
      diff.spans should have length 4
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 0, 1, 0, 2)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 1, 2, 2, 3)
      diff.spans(2) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 2, 3, 3, 4)
      diff.spans(3) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 3, 5, 4, 6)
    }

    "works with different cost matrix" in {
      val cfg = TextDiffCfg(
        insertCost = 3,
        deleteCost = 3
      )
      val diff = new TextDiffCalculator(cfg).calculate("私たち", "わたしたち")
      diff.score shouldBe 3
      diff.spans should have length 2
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 0, 1, 0, 3)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 1, 3, 3, 5)
    }

    "works with different cost matrix and お届け:届ける" in {
      val cfg = TextDiffCfg(
        insertCost = 1,
        deleteCost = 1
      )
      val differ = new TextDiffCalculator(cfg)
      val diff = differ.calculate("お届け", "届ける")
      //differ.printMatrix("お届け", "届ける")
      diff.score shouldBe 2
      diff.spans should have length 3
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_DELETE, 0, 1, 0, 0)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 1, 3, 0, 2)
      diff.spans(2) shouldBe DiffSpan(DiffTypes.TRANSITION_INSERT, 3, 3, 2, 3)
    }


    "works with different cost matrix and 今まで:いままで" in {
      val cfg = TextDiffCfg(
        insertCost = 3,
        deleteCost = 3
      )
      val differ = new TextDiffCalculator(cfg)
      val diff = differ.calculate("今まで", "いままで")
      //differ.printMatrix("今まで", "いままで")
      diff.score shouldBe 2
      diff.spans should have length 2
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 0, 1, 0, 2)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 1, 3, 2, 4)
    }

    "works with different cost matrix and ご愁傷様でございます" in {
      val cfg = TextDiffCfg(
        insertCost = 3,
        deleteCost = 3
      )
      val differ = new TextDiffCalculator(cfg)
      val diff = differ.calculate("ご愁傷様でございます", "ごしゅうしょうさまでございます")
      //differ.printMatrix("ご愁傷様でございます", "ごしゅうしょうさまでございます")
      diff.score shouldBe 8
      diff.spans should have length 3
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 0, 1, 0, 1)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 1, 4, 1, 9)
      diff.spans(2) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 4, 10, 9, 15)
    }

    "works with different cost matrix and 火の中水の中" in {
      val cfg = TextDiffCfg(
        insertCost = 3,
        deleteCost = 3
      )
      val differ = new TextDiffCalculator(cfg)
      val diff = differ.calculate("火の中水の中", "ひのなかみずのなか")
      //differ.printMatrix("火の中水の中", "ひのなかみずのなか")
      diff.score shouldBe 7
      diff.spans should have length 5
      diff.spans(0) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 0, 1, 0, 1)
      diff.spans(1) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 1, 2, 1, 2)
      diff.spans(2) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 2, 4, 2, 6)
      diff.spans(3) shouldBe DiffSpan(DiffTypes.TRANSITION_EQUALS, 4, 5, 6, 7)
      diff.spans(4) shouldBe DiffSpan(DiffTypes.TRANSITION_REPLACE, 5, 6, 7, 9)
    }
  }
}
