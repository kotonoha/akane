package ws.kotonoha.akane.parser

import ws.kotonoha.akane.helpers.lisp._
import ws.kotonoha.akane.resources.Classpath

import scala.util.parsing.input.CharSequenceReader

/**
 * @param subtypes
 * @author eiennohito
 * @since 15/08/15
 */
case class JumanPosType(num: Int, name: String, subtypes: Array[JumanPosSubtype])

case class JumanPosSubtype(num: Int, name: String, possibleConjs: Array[Int])

case class JumanConjType(num: Int, name: String, conjugations: Array[JumanConjForm])

case class JumanConjForm(num: Int, name: String, writing: String, reading: String)

class JumanPosSet(
  val pos: Array[JumanPosType],
  val conjugatons: Array[JumanConjType]
)

object JumanPosSet {
  lazy val default = JumanPosReader.fromClasspath()
}

object JumanPosReader {
  private def parseLisp(cont: String): List[KList] = {
    val input = new CharSequenceReader(cont)
    LispParser.lists(input) match {
      case LispParser.Success(x, _) => x
      case y => throw new JumanPosException("could not parse file:\n" + y + "\n\n" + cont)
    }
  }

  def fromClasspath() = {
    val conjFile = Classpath.fileAsString("juman/JUMAN.katuyou")
    val gramFile = Classpath.fileAsString("juman/JUMAN.grammar")
    val relFile = Classpath.fileAsString("juman/JUMAN.kankei")

    fromLispAst(parseLisp(gramFile), parseLisp(relFile), parseLisp(conjFile))
  }

  def fromLispAst(grammar: List[KList], kankei: List[KList], katuyou: List[KList]) = {
    val cobj = parseKatuyou(katuyou)
    val cmap = cobj.map {
      case (k, v) => k -> v.toArray
    }.toMap

    val ckeys = "*" :: cobj.map(_._1)

    val carray = ckeys.zipWithIndex.map {
      case (k, i) => cmap.get(k).map(a => JumanConjType(i, k, a)).getOrElse(
        JumanConjType(i, k, Array(JumanConjForm(0, "*", "", "")))
      )
    }

    val cmap2 = carray.map{o => o.name -> o.num}.toMap

    val kankobj = parseKankei(kankei)

    val posobj = ("*" -> List("*" -> false)) :: parseGrammar(grammar)

    val kanMap = kankobj.toMap

    val pobjs = posobj.zipWithIndex.map{
      case ((k, l), i) => JumanPosType(i, k, l.zipWithIndex.map {
        case ((nm, flag), j) =>
          val cobjs = kanMap.getOrElse((k, nm), Nil).map(cmap2).toArray
          JumanPosSubtype(j, nm, cobjs)
      }.toArray)
    }

    new JumanPosSet(pobjs.toArray, carray.toArray)
  }

  def conjugations(pairs: List[KElement]) = {
    var idx = 1
    pairs.map {
      case KItems(KAtom(l), KAtom(r)) =>
        val c = JumanConjForm(idx, l, r, r)
        idx += 1
        c
      case KItems(KAtom(l), KAtom(w), KAtom(r)) =>
        val c = JumanConjForm(idx, l, w, r)
        idx += 1
        c
      case x =>
        throw new JumanPosException("unknown format\n" + x)
    }
  }

  def parseKatuyou(lisp: List[KList]) = {
    lisp.map {
      case KItems(KAtom(name), KList(pairs)) => name -> (JumanConjForm(0, "*", "", "") :: conjugations(pairs))
      case x => throw new JumanPosException("unknown format\n" + x)
    }
  }

  def parseGrammar(lst: List[KList]) = {
    lst.map {
      case KItems(KItems(KAtom(nm))) => nm -> List(("*", false))
      case KItems(KItems(KAtom(nm), KAtom("%"))) => nm -> List(("*", true))
      case KItems(KItems(KAtom(nm)), KList(itms)) => nm -> (
        ("*", false) :: itms.map {
          case KItems(KAtom(s)) => (s, false)
          case KItems(KAtom(s), KAtom("%")) => (s, true)
        })
    }
  }

  def parseKankei(lst: List[KList]) = lst map {
    case KItems(KItems(KAtom(key1)), KList(items)) =>
      (key1, "*") -> ("*" :: items.map {
        case KAtom(cnt) => cnt
        case x => throw new JumanPosException("unknown format\n" + x)
      })
    case KItems(KItems(KAtom(key1), KAtom(key2)), KList(items)) =>
      (key1, key2) -> ("*" :: items.map {
        case KAtom(cnt) => cnt
        case x => throw new JumanPosException("unknown format\n" + x)
      })
    case x =>
      throw new JumanPosException("unknown format\n" + x)
  }
}

class JumanPosException(s: String) extends RuntimeException(s)
