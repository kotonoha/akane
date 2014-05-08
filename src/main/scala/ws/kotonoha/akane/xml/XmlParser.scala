/*
 * Copyright 2012 eiennohito
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ws.kotonoha.akane.xml

import javax.xml.stream.XMLEventReader
import collection.mutable.Stack
import javax.xml.stream.events._
import scala.None
import ws.kotonoha.akane.utils.CalculatingIterator
import scala.collection.mutable


trait XmlData {
  def data: String

  private var where: List[String] = Nil

  private[xml] def at(pos: List[String]): XmlData = {
    where = pos
    this
  }

  lazy val pos = where.reverse

  def revpos = where
}

case class XmlText(data: String) extends XmlData

case class XmlERef(data: String) extends XmlData

case class XmlEl(data: String) extends XmlData {
  var attrs = Map[String, String]()

  def this(data: String, attrmap: Map[String, String]) = {
    this(data)
    attrs = attrmap
  }

  def apply(key: String) = attrs(key)
}

case class XmlElEnd(data: String) extends XmlData

private[xml] class ParserState {

  def push(name: String) = {
    if (stack.isEmpty) {
      stack.push(List(name))
    } else {
      val t = top
      stack.push(name :: t)
    }
  }

  def pop() = stack.pop()

  val stack = mutable.Stack[List[String]]()

  def top = stack.top
}

class XmlIterator(in: XMLEventReader) extends CalculatingIterator[XmlData] {
  private lazy val state: ParserState = new ParserState

  private def extractAttributeMap(st: StartElement): Map[String, String] = {
    import scala.collection.JavaConversions._
    val iter = st.getAttributes
    if (!iter.hasNext) {
      return Map.empty
    }
    iter.map(_.asInstanceOf[Attribute])
      .foldLeft(Map[String, String]())((m, a) => m.updated(a.getName.getLocalPart, a.getValue))
  }

  private def transform(next: XMLEvent): Option[XmlData] = {
    next match {
      case st: StartElement => {
        val map = extractAttributeMap(st)
        val name = st.getName.getLocalPart
        state.push(name)
        Some(new XmlEl(name, map) at (state.top))
      }
      case en: EndElement => {
        val name = en.getName.getLocalPart
        val path = state.pop() // TODO:check equality
        Some(XmlElEnd(name) at (path))
      }
      case t: Characters => {
        var its: List[String] = t.getData :: Nil
        while (in.peek() match {
          case t: Characters => its = t.getData :: its; in.nextEvent(); true
          case _ => false
        }) {}
        Some(XmlText(its.reverse.mkString) at (state.top))
      }
      case er: EntityReference => Some(XmlERef(er.getName) at (state.top))
      case _ => None
    }
  }

  protected def calculate(): Option[XmlData] = {
    var ok = true
    while (ok && in.hasNext) {
      val next = in.nextEvent()
      val ev = transform(next)
      ok = ev.isEmpty
      if (!ok) {
        return ev
      }
    }
    None
  }
}

class XmlParseTransformer(in: CalculatingIterator[XmlData]) {
  def head = in.head

  def self = this

  def assertTag(s: String) = {
    assert(in.head.data.equals(s), s"${head} should equal to $s")
  }


  def next() = in.next()

  def selector[T](pf: PartialFunction[XmlData, T]) = {
    while (in.hasNext) {
      val n = in.head
      if (pf.isDefinedAt(n)) {
        pf.apply(n)
      } else {
        in.next()
      }
      //if (in.hasNext) in.next()
    }
  }

  def skipTo(tag: String) = {
    while (in.hasNext && in.head.data != tag) {
      val n = in.next()
      println(s"warning: skipped $n")
    }
  }

  def transSeq[T](name: String)(processor: XmlParseTransformer => T): Iterator[T] = new CalculatingIterator[T] {
    protected def calculate(): Option[T] = {
      val work = true
      while (work && in.hasNext) {
        val n = in.next()
        n match {
          case XmlEl(nm) if nm == name => {
            return Some(processor(untilEndTag(name)))
          }
          case _ => //do nothing
        }
      }
      None
    }
  }

  def transOnly[T](name: String)(p: XmlParseTransformer => T): Iterator[T] = new CalculatingIterator[T] {
    protected def calculate() = {
      in.head match {
        case XmlEl(`name`) => Some(trans(name)(p))
        case _ => None
      }
    }
  }

  def traverse[T](name: String)(p: XmlParseTransformer => T): Iterator[T] = new CalculatingIterator[T] {
    protected def calculate() = {
      in.head match {
        case XmlEl(`name`) => Some(p(self))
        case _ => None
      }
    }
  }

  def transSeq[T](name: String, noattr: Boolean)(processor: XmlParseTransformer => T): Iterator[T] = new CalculatingIterator[T] {
    protected def calculate(): Option[T] = {
      val work = true
      while (work && in.hasNext) {
        val n = in.next()
        n match {
          case x @ XmlEl(nm) if nm == name && (!noattr || x.attrs.size == 0) => {
            return Some(processor(untilEndTag(name)))
          }
          case _ => //do nothing
        }
      }
      None
    }
  }

  def trans[T](name: String)(body: XmlParseTransformer => T): T = {
    val work = true
    while (work && in.hasNext) {
      val n = in.next()
      n match {
        case XmlEl(`name`) => {
          val inp = untilEndTag(name)
          val data = body(inp)
          head match {
            case XmlElEnd(`name`) => in.next()
            case _ => //
          }
          return data
        }
        case _ => //do nothing
      }
    }
    throw new Exception("There wasn't tag " + name)
  }

  def transOpt[T](name: String)(body: XmlParseTransformer => T): Option[T] = {
    in.head match {
      case XmlEl(`name`) => {
        in.next()
        val res = Some(body(untilEndTag(name)))
        head match {
          case XmlElEnd(`name`) => in.next()
          case _ =>
        }
        res
      }
      case _ => None
    }
  }

  def skipAll() {
    while (in.hasNext) {
      val n = in.next()
      println(s"warning: skipped $n")
    }
  }

  def optTextOf(name: String): Option[String] = {
    in.head match {
      case XmlEl(`name`) => Some(textOf(name))
      case _ => None
    }
  }

  def textOf(name: String) = {
    val first = in.next()

    first match {
      case XmlEl(`name`) =>
        val sec = in.next()
        val cont = sec match {
          case XmlText(t) => Some(t)
          case XmlERef(t) => Some(t)
          case XmlElEnd(`name`) => None
          case _ => throw new JMDictParseException(s"error when parsing xml: invalid sequence $first, $sec")
        }
        cont match {
          case None => ""
          case Some(x) =>
            val third = in.next()
            third match {
              case XmlElEnd(`name`) => x
              case _ => throw new JMDictParseException(s"invalid closing tag when parsing xml: $first, $sec, $third")
            }
        }
      case _ => throw new JMDictParseException(s"first element is not an opening tag $first")
    }
  }

  def untilEndTag(name: String) = {
    new XmlParseTransformer(new CalculatingIterator[XmlData] {
      protected def calculate() = None

      override def chead = in.chead

      override def head = in.head

      override def next() = in.next()

      override def hasNext = chead match {
        case None => false
        case Some(XmlElEnd(nm)) if nm == name => false
        case _ => true
      }
    })
  }
}

class JMDictParseException(msg: String) extends RuntimeException(msg)

object XmlParser {
  implicit def iterator2parsetransformer(in: CalculatingIterator[XmlData]) = new XmlParseTransformer(in)

  def parse(in: XMLEventReader) = new XmlIterator(in)
}
