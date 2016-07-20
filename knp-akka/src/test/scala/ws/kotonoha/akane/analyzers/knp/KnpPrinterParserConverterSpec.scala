package ws.kotonoha.akane.analyzers.knp

import java.io.{BufferedReader, StringReader}

import org.scalatest.{FreeSpec, Matchers}
import ws.eiennohito.nlp.tree.KnpTrees
import ws.kotonoha.akane.parser.{JumanPosSet, KnpTabFormatParser}

/**
 * @author eiennohito
 * @since 2015/09/18
 */
class KnpPrinterParserConverterSpec extends FreeSpec with KnpTrees with Matchers {

  "TreeProcessing" - {
    val parser = new KnpTabFormatParser

    "read -> toProtobuf -> print -> toProtobuf -> print has protobuf messages equal" in {
      val init = resTreeOld("trees/test0")
      val pbuf1 = TableConverter.fromOld(init)

      val tprinter = new TablePrinter(JumanPosSet.default)
      val sbldr1 = new StringBuffer()
      tprinter.appendTable(sbldr1, pbuf1)
      val s1 = sbldr1.toString

      val parsed = parser.parse(new BufferedReader(new StringReader(s1))).get

      val pbuf2 = TableConverter.fromOld(parsed)

      pbuf1 shouldBe pbuf2
    }
  }

}
