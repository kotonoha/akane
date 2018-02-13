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

import java.io.{InputStream, InputStreamReader, Reader}
import java.nio.charset.Charset
import javax.xml.stream.{XMLEventReader, XMLInputFactory}
import javax.xml.stream.events._

import com.typesafe.scalalogging.StrictLogging
import ws.kotonoha.akane.io.Charsets
import ws.kotonoha.akane.utils.CalculatingIterator

import scala.annotation.tailrec
import scala.collection.mutable
import scala.language.implicitConversions

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
  var attrs: collection.Map[String, String] = Map.empty

  def this(data: String, attrmap: collection.Map[String, String]) = {
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

class XmlParserException(msg: String, inner: Throwable) extends RuntimeException(msg, inner)

class XmlIterator(in: XMLEventReader) extends CalculatingIterator[XmlData] {
  private lazy val state: ParserState = new ParserState

  def die(inner: Throwable) = {
    val el = in.peek()
    val loc = el.getLocation
    val line = loc.getLineNumber
    val msg = s"error in parsing at line $line;\nstate: ${state.stack}"
    throw new XmlParserException(msg, inner)
  }

  private def extractAttributeMap(st: StartElement): collection.Map[String, String] = {

    val iter = st.getAttributes
    if (!iter.hasNext) {
      return Map.empty
    }
    val bldr = new mutable.HashMap[String, String]

    while (iter.hasNext) {
      val i = iter.next().asInstanceOf[Attribute]
      bldr.put(i.getName.getLocalPart, i.getValue)
    }

    bldr
  }

  private def transform(next: XMLEvent): Option[XmlData] = {
    next match {
      case st: StartElement => {
        val map = extractAttributeMap(st)
        val name = st.getName.getLocalPart
        state.push(name)
        Some(new XmlEl(name, map).at(state.top))
      }
      case en: EndElement => {
        val name = en.getName.getLocalPart
        val path = state.pop() // TODO:check equality
        Some(XmlElEnd(name).at(path))
      }
      case t: Characters => {
        var its: List[String] = t.getData :: Nil
        while (in.peek() match {
                 case t: Characters => its = t.getData :: its; in.nextEvent(); true
                 case _             => false
               }) {}
        Some(XmlText(its.reverse.mkString).at(state.top))
      }
      case er: EntityReference => Some(XmlERef(er.getName).at(state.top))
      case _                   => None
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

class XmlParseTransformer(in: CalculatingIterator[XmlData], debug: Boolean = false)
    extends StrictLogging {

  def head = in.head

  def self = this

  def assertTag(s: String) = {
    assert(in.head.data.equals(s), s"${head} should equal to $s")
  }

  def next() = in.next()

  final def selector[T](pf: PartialFunction[XmlData, T]) = {
    while (in.hasNext) {
      val n = in.head
      if (pf.isDefinedAt(n)) {
        pf.apply(n)
        if (debug && in.chead.isDefined && (in.chead.get.eq(n))) {
          throw new XmlParseException(s"iterator should move forward, current element was ${n}")
        }
      } else {
        in.next()
      }
      //if (in.hasNext) in.next()
    }
  }

  def selectOne[T](pf: PartialFunction[XmlData, T]) = {
    val item = in.next()
    pf(item)
  }

  def skipTag() = {
    @tailrec
    def rec(it: CalculatingIterator[XmlData], target: String, levels: Int): Unit = {
      val local = it.next()
      local match {
        case x: XmlElEnd if x.data == target =>
          if (levels > 0) rec(it, target, levels - 1)
        case x: XmlEl if x.data == target =>
          rec(it, target, levels + 1)
        case _ =>
          rec(it, target, levels)
      }
    }

    in.next() match {
      case tag: XmlEl =>
        rec(in, tag.data, 0)
      case x =>
        throw new XmlParseException(s"head element $x was not a tag")
    }

  }

  def skipTo(tag: String) = {
    while (in.hasNext && in.head.data != tag) {
      val n = in.next()
      println(s"warning: skipped $n")
    }
  }

  def transSeq[T](name: String)(processor: XmlParseTransformer => T): Iterator[T] = {
    transformSeqFromTag(name)((_, it) => processor(it))
  }

  def transformSeqFromTag[T](name: String)(body: (XmlEl, XmlParseTransformer) => T): Iterator[T] =
    new CalculatingIterator[T] {
      override protected def calculate(): Option[T] = {
        val work = true
        while (work && in.hasNext) {
          val n = in.next()
          n match {
            case x @ XmlEl(`name`) => {
              return Some(body(x, untilEndTag(name)))
            }
            case _ => //do nothing
          }
        }
        None
      }
    }

  def transOnly[T](name: String)(p: XmlParseTransformer => T): Iterator[T] =
    new CalculatingIterator[T] {
      protected def calculate() = {
        in.head match {
          case XmlEl(`name`) => Some(trans(name)(p))
          case _             => None
        }
      }
    }

  def traverse[T](name: String)(p: XmlParseTransformer => T): Iterator[T] =
    new CalculatingIterator[T] {
      protected def calculate() = {
        in.head match {
          case XmlEl(`name`) => Some(p(self))
          case _             => None
        }
      }
    }

  def transSeq[T](name: String, noattr: Boolean)(processor: XmlParseTransformer => T): Iterator[T] =
    new CalculatingIterator[T] {
      protected def calculate(): Option[T] = {
        val work = true
        while (work && in.hasNext) {
          val n = in.next()
          n match {
            case x @ XmlEl(nm) if nm == name && (!noattr || x.attrs.isEmpty) => {
              return Some(processor(untilEndTag(name)))
            }
            case _ => //do nothing
          }
        }
        None
      }
    }

  final def trans[T](name: String)(body: XmlParseTransformer => T): T = {
    transformFromTag(name) { (_, inp) =>
      body(inp)
    }
  }

  final def transformFromTag[T](name: String)(body: (XmlEl, XmlParseTransformer) => T): T = {
    val work = true
    while (work && in.hasNext) {
      val n = in.next()
      n match {
        case el @ XmlEl(`name`) =>
          val inp = untilEndTag(name)
          val data = body(el, inp)
          head match {
            case XmlElEnd(`name`) => in.next()
            case _                => //
          }
          return data
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
          case _                =>
        }
        res
      }
      case _ => None
    }
  }

  def skipAll() {
    while (in.hasNext) {
      val n = in.next()
      logger.warn(s"skipped $n")
    }
  }

  def optTextOf(name: String): Option[String] = {
    in.head match {
      case XmlEl(`name`) => Some(textOf(name))
      case _             => None
    }
  }

  def content(): String = {
    val tag = in.head
    textOf(tag.data)
  }

  @inline
  final def contAttrs(): (String, collection.Map[String, String]) = {
    val tag = in.head
    textAndAttrs(tag.data)
  }

  def textOf(name: String) = {
    val first = in.next()

    first match {
      case XmlEl(`name`) =>
        val sec = in.next()
        val cont = sec match {
          case XmlText(t)       => t
          case XmlERef(t)       => t
          case XmlElEnd(`name`) => null
          case _ =>
            throw new XmlParseException(s"error when parsing xml: invalid sequence $first, $sec")
        }
        cont match {
          case null => ""
          case x =>
            val third = in.next()
            third match {
              case XmlElEnd(`name`) => x
              case _ =>
                throw new XmlParseException(
                  s"invalid closing tag when parsing xml: $first, $sec, $third")
            }
        }
      case _ => throw new XmlParseException(s"first element is not an opening tag $first")
    }
  }

  @inline
  final def textAndAttrs(name: String): (String, collection.Map[String, String]) = {
    in.head match {
      case e @ XmlEl(`name`) => (textOf(name), e.attrs)
      case _ =>
        throw new XmlParseException(s"first element is not an opening tag $name, but ${in.head}")
    }
  }

  def untilEndTag(name: String) = {
    new XmlParseTransformer(
      new CalculatingIterator[XmlData] {
        protected def calculate() = None

        override def chead = in.chead

        override def head = in.head

        override def next() = in.next()

        override def hasNext = chead match {
          case None                   => false
          case Some(XmlElEnd(`name`)) => false
          case _                      => true
        }
      },
      this.debug
    )
  }
}

object XmlParser {
  implicit def iterator2parsetransformer(in: CalculatingIterator[XmlData]): XmlParseTransformer =
    new XmlParseTransformer(in, debug = false)

  def parse(in: XMLEventReader) = new XmlIterator(in)

  def create(in: InputStream, cs: Charset = Charsets.utf8): XmlIterator = {
    val rdr = new InputStreamReader(in, cs)
    create(rdr)
  }

  def create(in: Reader): XmlIterator = {
    val fact = XMLInputFactory.newInstance()
    fact.setProperty(XMLInputFactory.IS_VALIDATING, false)
    fact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES, false)

    val reader = fact.createFilteredReader(
      fact.createXMLEventReader(in),
      WhitespaceFilter
    )

    XmlParser.parse(reader)
  }
}

class XmlParseException(msg: String) extends Exception(msg)
