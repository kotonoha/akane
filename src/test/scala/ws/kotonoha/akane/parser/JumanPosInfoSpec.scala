package ws.kotonoha.akane.parser

import java.io.InputStreamReader

import org.scalatest.{Matchers, FreeSpec}
import ws.kotonoha.akane.pipe.knp.lisp.LispParser

import scala.util.parsing.input.StreamReader
import scalax.io.Resource

/**
 * @author eiennohito
 * @since 15/08/15
 */
class JumanPosInfoSpec extends FreeSpec with Matchers {

  def parseLisp(res: String) = {
    Resource.fromClasspath(res, this.getClass).inputStream.acquireAndGet { is =>
      LispParser.lists.apply(StreamReader(new InputStreamReader(is, "utf-8"))) match {
        case LispParser.Success(x, _) => x
        case x => throw new Exception(x.toString)
      }
    }
  }

  "JumanPosParser" - {
    "parses katuyou" in {
      val lst = parseLisp("juman/JUMAN.katuyou")
      lst should not be ('empty)
      val obj = JumanPosReader.parseKatuyou(lst)
      obj should not be('empty)
      obj should have length 32
      obj(31)._1 shouldBe "動詞性接尾辞うる型"
    }
  }
}
