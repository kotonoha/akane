package ws.kotonoha.akane.kytea

import org.scalatest.{FreeSpec, Matchers}
import ws.kotonoha.akane.kytea.wire.{KyteaSentence, KyteaUnit}

/**
  * @author eiennohito
  * @since 2016/10/13
  */
class KyteaFormatSpec extends FreeSpec with Matchers {
  "KyteaFormat" - {
    val kc = KyteaConfig(None, wordBound = " ", tagBound = "/")
    "parses a single kytea morpheme" in {
      val fmt = new KyteaFormat(kc)
      val morph = "自分/名詞/じぶん"
      val parsed = fmt.parseMorpheme(morph)
      parsed.fields shouldBe Seq("自分", "名詞", "じぶん")
    }

    def ku(items: String*) = KyteaUnit(items)

    "parses two kytea morphemes" in {
      val fmt = new KyteaFormat(kc)
      val sent = "人生/名詞/じんせい は/助詞/は 麻薬/名詞/まやく"
      val parsed = fmt.parse(sent)
      parsed shouldBe KyteaSentence(
        Seq(
          ku("人生", "名詞", "じんせい"),
          ku("は", "助詞", "は"),
          ku("麻薬", "名詞", "まやく")
        )
      )
    }
  }
}
