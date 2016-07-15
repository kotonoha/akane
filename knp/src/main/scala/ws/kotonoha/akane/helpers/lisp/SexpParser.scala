/*
 * Copyright 2012-2016 eiennohito (Tolmachev Arseny)
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

package ws.kotonoha.akane.helpers.lisp

import org.apache.commons.lang3.StringUtils
import ws.kotonoha.akane.utils.StringUtil

import scala.annotation.tailrec
import scala.collection.mutable.ListBuffer

sealed trait ParseResult[+T] {
  @inline
  final def map[R](fn: T => R): ParseResult[R] = {
    this match {
      case s: ParseSuccess[T] => new ParseSuccess[R](fn(s.obj), s.pos)
      case pf: ParseFailure => pf
    }
  }

  def pos: Int
}
case class ParseSuccess[T](obj: T, pos: Int) extends ParseResult[T]
case class ParseFailure(msg: String, pos: Int) extends ParseResult[Nothing]


/**
  * @author eiennohito
  * @since 2015/12/14
  */
class SexpParser {
  def parseAtom(input: CharSequence, index: Int) = {
    val first = input.charAt(index)
    if (first == '"') {
      val end = StringUtils.indexOf(input, '"', index + 1)
      if (end == -1) {
        new ParseFailure("no end of quote", index)
      }
      ParseSuccess(new KAtom(input.subSequence(index + 1, end).toString), end + 1)
    } else {
      val end = StringUtil.indexOfAny(input, " )", index + 1)
      if (end == -1) {
        new ParseFailure("no end of atom", index)
      }
      val next = skipComments(input, end)
      ParseSuccess(new KAtom(
        input.subSequence(index, end).toString
      ), next)
    }
  }

  def skipComments(input: CharSequence, index: Int): Int = {
    var pos = index
    val end = input.length()

    var continue = true
    while (pos < end && continue) {
      val ch = input.charAt(pos)
      if (Character.isWhitespace(ch)) {
        pos += 1
      } else if (ch == ';') {
        val eol = StringUtils.indexOf(input, '\n', pos)
        if (eol == -1) {
          pos = end
          continue = false
        } else {
          pos = eol + 1
        }
      } else {
        continue = false
      }
    }
    pos
  }

  def parseList(input: CharSequence, index: Int): ParseResult[KList] = {
    val lbuf = new ListBuffer[KElement]

    @tailrec
    def rec(idx: Int): ParseResult[Unit] = {
      val ch = input.charAt(idx)
      if (ch == ')') {
        val next = skipComments(input, idx + 1)
        ParseSuccess((), next)
      } else {
        parseExpression(input, idx) match {
          case pf: ParseFailure => pf
          case ps: ParseSuccess[KElement] =>
            lbuf += ps.obj
            val start = skipComments(input, ps.pos)
            rec(start)
        }
      }
    }

    val start = skipComments(input, index)
    rec(start).map(_ => KList(lbuf.result()))
  }

  def parseExpression(input: CharSequence, index: Int): ParseResult[KElement] = {
    var idx = index
    val len = input.length()
    while (idx < len) {
      val c = input.charAt(idx)
      c match {
        case ';' => //comment
          val end = StringUtils.indexOf(input, '\n', idx)
          if (end == -1) {
            return ParseFailure("no end of comment", idx)
          }
          idx = end + 1
        case '(' => return parseList(input, idx + 1)
        case c if Character.isWhitespace(c) => idx += 1
        case _ => return parseAtom(input, idx)
      }
    }
    ParseFailure("end of input", index)
  }
}

case class SexpException(msg: String) extends RuntimeException(msg)
