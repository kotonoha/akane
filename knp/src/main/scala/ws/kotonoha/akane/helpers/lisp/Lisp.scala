package ws.kotonoha.akane.helpers.lisp

import ws.kotonoha.akane.utils.{XDouble, XInt}

import scala.util.parsing.combinator.RegexParsers

/**
 * @author eiennohito
 * @since 2013-09-04
 */
class Lisp {

}

sealed trait KElement
case class KAtom(content: String) extends KElement
case class KList(items: List[KElement]) extends KElement {
  override def toString = {
    val builder = new StringBuilder
    LispRenderer.renderList(this, builder)
    builder.result()
  }
}

object KItems {
  def apply(items: KElement*) = {
    new KList(items.toList)
  }
  def unapplySeq(lst: KList) = Some(lst.items)
}

object KInt {
  def unapply(x: KAtom): Option[Int] = XInt.unapply(x.content)
}

object KDouble {
  def unapply(x: KAtom): Option[Double] = XDouble.unapply(x.content)
}

object LispParser extends RegexParsers {

  override val whiteSpace = """(?:(?:\s+)|(?:[#;][^\n\r]*[\n\r])|(?:EOS))+""".r

  def quotedString = "(?:\"(?:[^\"\\\\]|\\\\.)*\")".r ^^ (s => KAtom(s.substring(1, s.length - 1)))

  def simpleAtom = "(?:[^\\s\\(\\)]+)".r ^^ KAtom

  def atom = quotedString | simpleAtom
  def lbracket = literal("(")
  def rbracket = literal(")")

  def list: Parser[KList] = (lbracket ~> rep(expression)) <~ rbracket ^^ KList

  def lists = rep(list)

  def nil = literal("NIL") ^^^ KList(Nil)

  def expression = list | nil | atom

  def expressionWithComments = opt(rep(whiteSpace)) ~> expression <~ opt(rep(whiteSpace))

  def parser = list
}

object LispRenderer {

  def renderByLines(data: TraversableOnce[KElement]) = {
    val bldr = new StringBuilder
    data.foreach { d =>
      render(d, bldr)
      bldr.append("\n")
    }
    bldr.result()
  }

  def renderAtom(atom: KAtom, builder: StringBuilder) = {
    if (atom.content.contains(" ")) {
      builder.append('\"').append(atom.content).append('\"').append(" ")
    } else builder.append(atom.content).append(" ")
  }

  def renderList(list: KList, builder: StringBuilder) = {
    builder.append('(')
    list.items.foreach {
      i => render(i, builder)
    }
    if (builder.last == ' ') {
      builder(builder.length - 1) = ')'
    } else builder.append(')')
  }

  def render(elem: KElement, bldr: StringBuilder): Unit = {
    elem match {
      case e: KAtom => renderAtom(e, bldr)
      case e: KList => renderList(e, bldr)
    }
  }
}



