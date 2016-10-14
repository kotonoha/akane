package ws.kotonoha.akane.utils

import org.scalatest.{FreeSpec, Matchers}

/**
 * @author eiennohito
 * @since 15/08/14
 */
class IntBufferSpec extends FreeSpec with Matchers {
  "int buffer" - {
    "when added 5 elements" - {
      val buf = new IntBuffer
      buf.append(1 to 5)
      buf should have length 5

      buf(0) shouldBe 1
      buf(1) shouldBe 2
      buf(2) shouldBe 3
      buf(3) shouldBe 4
      buf(4) shouldBe 5
    }
  }
}
