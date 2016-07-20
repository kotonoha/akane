package ws.kotonoha.akane.analyzers.knp

import org.scalatest.{FreeSpec, Matchers}
import ws.kotonoha.akane.analyzers.juman.{JumanOption, JumanPos, JumanText}

/**
 * @author eiennohito
 * @since 2015/09/10
 */
class JumanTextSpec extends FreeSpec with Matchers {
  "JumanText" - {
    "parseOptions" - {
      "parses normal line" in {
        val line = """wtf"代表表記:機関/きかん カテゴリ:組織・団体"wtf"""
        val opts = JumanText.parseOptions(line, 3, line.length - 3)
        opts should have length 2
        opts(0) shouldBe JumanOption("代表表記", Some("機関/きかん"))
        opts(1) shouldBe JumanOption("カテゴリ", Some("組織・団体"))
      }

      "parses single value" in {
        val line = """wtf"代表表記:最強だ/さいきょうだ"wtf"""
        val opts = JumanText.parseOptions(line, 3, line.length - 3)
        opts should have length 1
        opts(0) shouldBe JumanOption("代表表記", Some("最強だ/さいきょうだ"))
      }

      "parses values without semicolons" in {
        val line = """wtf"代表表記:池/いけ 漢字読み:訓 地名末尾 カテゴリ:場所-自然"wtf"""
        val opts = JumanText.parseOptions(line, 3, line.length - 3)
        opts should have length 4
        opts(2) shouldBe JumanOption("地名末尾", None)
      }

      "parses NIL" in {
        val line = "NIL"
        val opts = JumanText.parseOptions(line, 0, line.length)
        opts should be ('empty)
      }
    }

    "parseLine" - {
      "parses one line" in {
        val line = """乗って のって 乗る 動詞 2 * 0 子音動詞ラ行 10 タ系連用テ形 14 "代表表記:乗る/のる 自他動詞:他:乗せる/のせる 反義:動詞:降りる/おりる""""
        val lex = JumanText.parseLine(line, 0, line.length)
        lex.surface shouldBe "乗って"
        lex.reading shouldBe "のって"
        lex.baseform shouldBe "乗る"
        lex.posInfo shouldBe JumanPos(2, 0, 10, 14)
        lex.options should have length 3
      }
    }
  }
}
