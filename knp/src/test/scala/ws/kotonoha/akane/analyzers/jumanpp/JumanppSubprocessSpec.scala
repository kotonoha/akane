package ws.kotonoha.akane.analyzers.jumanpp

import java.io.ByteArrayOutputStream

import org.scalatest.{FreeSpec, Matchers}
import ws.kotonoha.akane.io.Charsets

/**
  * @author eiennohito
  * @since 2016/09/28
  */
class JumanppSubprocessSpec extends FreeSpec with Matchers {
  "JumanppSubprocess.Output" - {
    "correctly appends end-of-line character" in {
      val out = new JumanppSubprocess.JumanppInput()
      val baos = new ByteArrayOutputStream()
      out.writeTo(baos, "test")
      val data = new String(baos.toByteArray, Charsets.utf8)
      data shouldBe "test\n"
    }
  }
}
