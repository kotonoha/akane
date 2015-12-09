package ws.kotonoha.akane.analyzers.kytea

import org.scalatest.{Matchers, FreeSpec}

import scala.collection.mutable.ArrayBuffer

/**
  * @author eiennohito
  * @since 2015/12/09
  */
class KyteaReaderSpec extends FreeSpec with Matchers {
  "KyteaReaderSpec" - {
    "parses small fragment" in {
      val data = "一/名詞/いち 度/名詞/ど 寝言/名詞/ねごと を/助詞/を 言/動詞/い う/語尾/う"
      val reader = new KyteaReader(' ', '/', 3)
      val buf = new ArrayBuffer[RawMorpheme]()
      reader.readTo(data, buf)
      buf should have length 6
    }
  }
}
