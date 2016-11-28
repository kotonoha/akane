package ws.kotonoha.akane.analyzers.jumanpp

import org.scalatest.{FreeSpec, Matchers}
import ws.kotonoha.akane.analyzers.juman.JumanPos
import ws.kotonoha.akane.juman.PosCodec
import ws.kotonoha.akane.parser.{JumanConjForm, JumanConjType, JumanPosSubtype}

/**
  * @author eiennohito
  * @since 2016/11/28
  */

object PosCodecSpec {
  implicit class OrEmptyOp[T](val s: Array[T]) extends AnyVal {
    def orSeq(other: => T): Seq[T] = {
      if (s.isEmpty) Seq(other) else s
    }
  }
}

class PosCodecSpec extends FreeSpec with Matchers {
  import PosCodecSpec._

  "PosCodec" - {
    "raw encode-decode cycle works for all pos" in {
      import ws.kotonoha.akane.parser.JumanPosSet.{default => p}

      for {
        pos <- p.pos
        sub <- pos.subtypes.orSeq(JumanPosSubtype(0, "*", Array.emptyIntArray))
        ctype <- sub.possibleConjs.map(p.conjugatons.apply).orSeq(JumanConjType(0, "*", Array.empty))
        cform <- ctype.conjugations.orSeq(JumanConjForm(0, "*", "", ""))
      } {
        val posObj = JumanPos(pos.num, sub.num, ctype.num, cform.num)
        val code = PosCodec.encodeRaw(posObj)
        val decoded = PosCodec.decodeRaw(code)
        posObj shouldBe decoded
      }
    }

    "combined encode-decode works for all pos" in {
      import ws.kotonoha.akane.parser.JumanPosSet.{default => p}
      val codec = PosCodec.default

      for {
        pos <- p.pos
        sub <- pos.subtypes.orSeq(JumanPosSubtype(0, "*", Array.emptyIntArray))
        ctype <- sub.possibleConjs.map(p.conjugatons.apply).orSeq(JumanConjType(0, "*", Array.empty))
        cform <- ctype.conjugations.orSeq(JumanConjForm(0, "*", "", ""))
      } {
        val posObj = JumanPos(pos.num, sub.num, ctype.num, cform.num)
        val code = codec.encode(posObj)
        val decoded = codec.decode(code)
        posObj shouldBe decoded
      }
    }
  }
}
