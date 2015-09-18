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

  def checkPos(set: JumanPosSet, tr: KnpTable): Unit = {
    for (l <- tr.lexemes) {
      val p = l.pos
      val pos = p.partOfSpeech
      val px = set.pos(pos.id)
      pos.name shouldBe px.name

      val sub = p.subPart
      sub.name shouldBe px.subtypes(sub.id).name

      val conjTp = p.conjType
      val ctx = set.conjugatons(conjTp.id)
      conjTp.name shouldBe ctx.name

      val conjFrm = p.conjForm
      conjFrm.name shouldBe ctx.conjugations(conjFrm.id).name
    }
  }

  "JumanPosReader" - {
    "parses katuyou" in {
      val lst = parseLisp("juman/JUMAN.katuyou")
      lst should not be ('empty)
      val obj = JumanPosReader.parseKatuyou(lst)
      obj should not be('empty)
      obj should have length 32
      obj(31)._1 shouldBe "動詞性接尾辞うる型"
    }

    "parses kankei" in {
      val lst = parseLisp("juman/JUMAN.kankei")
      lst should not be('empty)
      val obj = JumanPosReader.parseKankei(lst)
      obj should have length 7
      obj(6)._1 shouldBe ("接尾辞", "動詞性接尾辞")
    }

    "parses grammar" in {
      val lst = parseLisp("juman/JUMAN.grammar")
      val obj = JumanPosReader.parseGrammar(lst)
      obj should have length 15
    }
  }

  "default JumanPosSet" - {
    lazy val set = JumanPosSet.default
    "correctly loads" in {
      set.pos should have length 16
      set.conjugatons should have length 33
    }

    "and matches all pos tags" - {
      "tree: tree0" in {
        val tr = TreeUtil.classpath("trees/bunsetsu-1.txt")
        checkPos(set, tr)
      }

      "tree: tree1" in {
        checkPos(set, TreeUtil.classpath("trees/weirdKihonku.txt"))
      }

      "tree: tree2" in {
        checkPos(set, TreeUtil.classpath("knp.tab.txt"))
      }
    }
  }
}
