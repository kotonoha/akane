package ws.kotonoha.akane.parser

import ws.kotonoha.akane.pipe.knp.lisp.{KAtom, KItems, KList, KElement}

/**
 * @author eiennohito
 * @since 15/08/15
 */
case class JumanPosInfo(num: Int, name: String, subtypes: Array[JumanPosSubtype])

case class JumanPosSubtype(num: Int, name: String, conjugations: Array[JumanConjugatable])

case class JumanConjugatable(num: Int, name: String, conjugations: Array[JumanConjugation])

case class JumanConjugation(num: Int, name: String, writing: String, reading: String)

class JumanPosSet {

}

object JumanPosSet {
  val default =  JumanPosReader.fromClasspath()
}

object JumanPosReader {
  def fromClasspath() = ???

  def fromLispAst(grammar: List[KList], kankei: List[KList], katuyou: List[KList]) {

  }

  def conjugations(pairs: List[KElement]) = {
    var idx = 1
    pairs.map {
      case KItems(KAtom(l), KAtom(r)) =>
        val c = JumanConjugation(idx, l, r, r)
        idx += 1
        c
      case KItems(KAtom(l), KAtom(w), KAtom(r)) =>
        val c = JumanConjugation(idx, l, w, r)
        idx += 1
        c
      case x =>
        throw new JumanPosException("unknown format\n" + x)
    }
  }

  def parseKatuyou(lisp: List[KList]) = {
    lisp.map {
      case KItems(KAtom(name), KList(pairs)) => name -> (JumanConjugation(0, "*", "", "") :: conjugations(pairs))
      case x => throw new JumanPosException("unknown format\n" + x)
    }
  }
}

class JumanPosException(s: String) extends RuntimeException(s)
