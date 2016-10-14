package ws.kotonoha.akane.utils.diff

import org.scalatest.{FreeSpec, Matchers}

/**
  * @author eiennohito
  * @since 2015/12/09
  */

class ISAlignableSeq(is: IndexedSeq[Int]) extends SegmentBorders {
  override def length = is.length
  override def apply(pos: Int) = is(pos)
}

class SequenceAlignerSpec extends FreeSpec with Matchers {
  "SequenceAligner" - {
    "finds two sequences equal" in {
      val s1 = new ISAlignableSeq(Array(1, 2, 5, 7))
      val s2 = new ISAlignableSeq(Array(1, 2, 5, 7))
      val alignment = new SequenceAligner(s1, s2).align()
      alignment.spans should not be 'empty
      alignment.fullMatch shouldBe true
    }

    "finds insertion" in {
      val s1 = new ISAlignableSeq(Array(1, 7))
      val s2 = new ISAlignableSeq(Array(1, 4, 7))
      val alignment = new SequenceAligner(s1, s2).align()
      alignment.spans should not be 'empty
      alignment.fullMatch shouldBe false
      alignment.inserts shouldBe 1
      alignment.deletes shouldBe 0
      alignment.replaces shouldBe 0
      alignment.spans should have length 3
    }

    "finds insertion2" in {
      val s1 = new ISAlignableSeq(Array(1, 2, 3, 7))
      val s2 = new ISAlignableSeq(Array(1, 2, 3, 4, 7))
      val alignment = new SequenceAligner(s1, s2).align()
      alignment.spans should not be 'empty
      alignment.fullMatch shouldBe false
      alignment.inserts shouldBe 1
      alignment.deletes shouldBe 0
      alignment.replaces shouldBe 0
      alignment.spans should have length 3
    }

    "finds deletion" in {
      val s1 = new ISAlignableSeq(Array(1, 4, 7))
      val s2 = new ISAlignableSeq(Array(1, 7))
      val alignment = new SequenceAligner(s1, s2).align()
      alignment.spans should not be 'empty
      alignment.fullMatch shouldBe false
      alignment.deletes shouldBe 1
      alignment.replaces shouldBe 0
      alignment.inserts shouldBe 0
      alignment.spans should have length 3
    }

    "finds replacement" in {
      val s1 = new ISAlignableSeq(Array(1, 4, 7))
      val s2 = new ISAlignableSeq(Array(1, 5, 7))
      val alignment = new SequenceAligner(s1, s2).align()
      alignment.spans should not be 'empty
      alignment.fullMatch shouldBe false
      alignment.deletes shouldBe 0
      alignment.replaces shouldBe 1
      alignment.inserts shouldBe 0
      alignment.spans should have length 3
      alignment.spans(1).kind shouldBe DiffTypes.TRANSITION_REPLACE
    }

    "finds replacement in first position" in {
      val s1 = new ISAlignableSeq(Array(2, 5, 7))
      val s2 = new ISAlignableSeq(Array(1, 5, 7))
      val alignment = new SequenceAligner(s1, s2).align()
      alignment.spans should not be 'empty
      alignment.fullMatch shouldBe false
      alignment.deletes shouldBe 0
      alignment.replaces shouldBe 1
      alignment.inserts shouldBe 0
      alignment.spans should have length 2
      alignment.spans.head.kind shouldBe DiffTypes.TRANSITION_REPLACE
    }

    "finds a insertion at back" in {
      val s1 = new ISAlignableSeq(Array(1, 5, 7))
      val s2 = new ISAlignableSeq(Array(1, 5, 7, 9))
      val alignment = new SequenceAligner(s1, s2).align()
      alignment.spans should not be 'empty
      alignment.fullMatch shouldBe false
      alignment.deletes shouldBe 0
      alignment.replaces shouldBe 0
      alignment.inserts shouldBe 1
      alignment.spans should have length 2
      alignment.spans.last.kind shouldBe DiffTypes.TRANSITION_INSERT
    }

    "finds a replacement at back" in {
      val s1 = new ISAlignableSeq(Array(1, 5, 7))
      val s2 = new ISAlignableSeq(Array(1, 5, 9))
      val alignment = new SequenceAligner(s1, s2).align()
      alignment.spans should not be 'empty
      alignment.fullMatch shouldBe false
      alignment.deletes shouldBe 0
      alignment.replaces shouldBe 1
      alignment.inserts shouldBe 0
      alignment.spans should have length 2
      alignment.spans.last.kind shouldBe DiffTypes.TRANSITION_REPLACE
    }

    "finds a long replacement at back" in {
      val s1 = new ISAlignableSeq(Array(1, 5, 7))
      val s2 = new ISAlignableSeq(Array(1, 5, 6, 9))
      val alignment = new SequenceAligner(s1, s2).align()
      alignment.spans should not be 'empty
      alignment.fullMatch shouldBe false
      alignment.deletes shouldBe 0
      alignment.replaces shouldBe 1
      alignment.inserts shouldBe 0
      alignment.spans should have length 2
      alignment.spans.last.kind shouldBe DiffTypes.TRANSITION_REPLACE
    }

    "finds two replacements" in {
      val s1 = new ISAlignableSeq(Array(2, 5, 7))
      val s2 = new ISAlignableSeq(Array(1, 5, 9))
      val alignment = new SequenceAligner(s1, s2).align()
      alignment.spans should not be 'empty
      alignment.fullMatch shouldBe false
      alignment.deletes shouldBe 0
      alignment.replaces shouldBe 2
      alignment.inserts shouldBe 0
      alignment.spans should have length 3
      alignment.spans.head.kind shouldBe DiffTypes.TRANSITION_REPLACE
      alignment.spans.last.kind shouldBe DiffTypes.TRANSITION_REPLACE
    }

  }
}
