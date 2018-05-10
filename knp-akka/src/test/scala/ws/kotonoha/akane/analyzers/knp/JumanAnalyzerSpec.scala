package ws.kotonoha.akane.analyzers.knp

import org.scalatest.{FreeSpec, Inside, Matchers}
import ws.kotonoha.akane.analyzers.juman.{JumanConfig, JumanSubprocess}
import ws.kotonoha.akane.resources.Classpath

import scala.util.{Failure, Success}

/**
 * @author eiennohito
 * @since 2015/09/10
 */
class JumanAnalyzerSpec extends FreeSpec with Matchers with Inside {
  "JumanSubprocess" - {
    "reader" - {
      "reads a juman file" in {
        for (is <- Classpath.inputStream("juman/strongest.txt")) {
          val rdr = JumanSubprocess.reader(new JumanConfig("", "utf-8", Nil))
          val obj = rdr.readFrom(is)
          inside(obj) {
            case Success(x) =>
              val lexs = x.morphemes
              lexs should have length 4
              lexs.head should have (
                'surface ("俺"),
                'reading ("おれ")
              )
            case Failure(x) => throw x
          }
        }
      }

      "reads file with variants" in {
        for (is <- Classpath.inputStream("juman/variants.txt")) {
          val rdr = JumanSubprocess.reader(new JumanConfig("", "utf-8", Nil))
          val obj = rdr.readFrom(is)
          inside(obj) {
            case Success(x) =>
              val lexs = x.morphemes
              lexs should have length 5
              lexs.head.variants should have length 2
            case Failure(x) => throw x
          }
        }
      }
    }
  }
}
