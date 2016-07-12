package ws.kotonoha.akane.pipe.knp.lisp

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers

import scala.util.parsing.input.{CharSequenceReader, Reader, StreamReader}
import java.io.InputStreamReader

import ws.kotonoha.akane.helpers.lisp.{KAtom, KItems, LispParser}

/**
 * @author eiennohito
 * @since 2013-09-04
 */
class LispParserTest extends FreeSpec with ShouldMatchers {

  implicit def input(s: String): Reader[Char] = {
    new CharSequenceReader(s, 0)
  }

  "lisp parser" - {
    "parses small list" in {
      val res = LispParser.list("(the info)")
      res match {
        case LispParser.Success(KItems(the, info), _) =>
          the should equal (KAtom("the"))
          info should equal (KAtom("info"))
      }
    }

    "parses a knp output" in {
      val resource = getClass.getClassLoader.getResourceAsStream("knp.answer.txt")
      val reader = new InputStreamReader(resource, "utf-8")
      val input = StreamReader.apply(reader)
      val result = LispParser.parser(input)
      result.successful should be (true)
      resource.close()
    }
  }

}
