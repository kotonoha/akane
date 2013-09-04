package ws.kotonoha.akane.pipe.knp.lisp

import scala.util.parsing.combinator.RegexParsers
import ws.kotonoha.akane.utils.XInt

/**
 * @author eiennohito
 * @since 2013-09-04
 */
class Lisp {

}

sealed trait KElement
case class KAtom(content: String) extends KElement
case class KList(items: List[KElement]) extends KElement
object KItems {
  def unapplySeq(lst: KList) = Some(lst.items)
}

object KInt {
  def unapply(x: KAtom) = XInt.unapply(x.content)
}

object LispParser extends RegexParsers {


  override val whiteSpace = """(\s+)|(#[^\n\r]*[\n\r])""".r

  def quotedString = "(?:\"(?:[^\"\\\\]|\\\\.)*\")".r ^^ (s => KAtom(s.substring(1, s.length - 1)))

  def simpleAtom = "(?:[^\\s\\(\\)]+)".r ^^ KAtom

  def atom = quotedString | simpleAtom
  def lbracket = literal("(")
  def rbracket = literal(")")

  def list: Parser[KList] = (lbracket ~> rep(expression)) <~ rbracket ^^ KList

  def nil = literal("NIL") ^^^ KList(Nil)

  def expression = (list | nil | atom)

  def parser = list
}


