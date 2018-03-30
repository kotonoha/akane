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

package ws.kotonoha.akane.parser

import com.typesafe.scalalogging.StrictLogging
import ws.kotonoha.akane.ast._
import java.nio.CharBuffer

import collection.mutable.ListBuffer
import ws.kotonoha.akane.ast.Image

import collection.immutable.HashSet
import ws.kotonoha.akane.unicode.UnicodeUtil

import scala.annotation.tailrec

/**
  * @author eiennohito
  * @since 16.08.12
  */
trait AozoraInput extends StrictLogging {
  def peek: Int //doesn't go forward
  def next: Int //goes forward
  def mark(): Unit
  def subseq(rel: Int): Option[CharSequence]

  def skipMatch(s: String) = {
    var i = 0
    val len = s.length
    while (i < len && s(i) == peek.toChar) {
      i += 1
      next
    }
    len == i
  }

  def skipUntil(c: Char, max: Int = -1) = {
    mark()
    if (max > 0) {
      var cnt = max
      while (c != peek && cnt > 0 && peek != -1) {
        next
        cnt -= 1
      }
    } else {
      while (c != peek && peek != -1) {
        next
      }
    }

    val s = subseq(-1)
    s match {
      case Some(s) => {
        val cnt = s.length()
        if (cnt > 100) {
          logger.warn(s"attention: long skip for $cnt, \n${s.toString}")
        }
      }
      case None => {
        logger.warn("Skipped very very *very* much, this is a bug!")
      }
    }
  }
}

object AozoraParser {
  val SENTENCE_SEPARATORS = HashSet('。', '？', '！', '?', '!', '」')
}

class AozoraParser(inp: AozoraInput) extends BufferedIterator[HighLvlNode] with StrictLogging {
  private val buf = CharBuffer.allocate(16 * 1024) // there shouldn't be sentences larger than 16kbtytes, rly

  private def content = {
    val str = new String(buf.array(), 0, buf.position())
    buf.clear()
    str
  }

  def parseImgTag(): Option[HighLvlNode] = {
    if (inp.skipMatch("<img src=\"")) {
      var cnt = 0
      while (inp.peek != '\"' && cnt < 250) {
        buf.append(inp.next.toChar)
        cnt += 1
      }
      if (cnt == 250) {
        inp.skipUntil('\n', 250)
        None
      } else {
        inp.skipUntil('>', 250)
        Some(Image(content))
      }
    } else {
      inp.skipUntil('>', 250)
      calculateNextNode()
    }
  }

  def parseEndline(): Option[HighLvlNode] = {
    (inp.next.toChar, inp.peek.toChar) match {
      case ('\n', '\r') => inp.next; Some(EndLine)
      case ('\r', '\n') => inp.next; Some(EndLine)
      case ('\n', _)    => Some(EndLine)
      case _            => calculateNextNode()
    }
  }

  def parseSystem(): Option[HighLvlNode] = {
    if (inp.skipMatch("［＃改ページ］")) {
      Some(PageBreak)
    } else {
      inp.skipUntil('］', 150)
      calculateNextNode()
    }
  }

  // | symbol commits string node, so we just need to find last non-kanji in buffer, it will be a border of ruby
  def handleRuby(bldr: ListBuffer[Node]): Unit = {
    inp.next
    val prev = content
    var cnt = 0
    while (inp.peek != '》' && cnt < 100) {
      buf.append(inp.next.toChar)
      cnt += 1
    }
    if (cnt == 100) {
      logger.warn(s"skipped 500 chars after:\n$prev\nchars are\n$content")
      inp.skipUntil('\n', 250)
    }
    var i = prev.length - 1
    while (i >= 0 && UnicodeUtil.isKanji(prev(i))) {
      i -= 1
    }
    if (i == -1) { //will be -1 if no chars here
      bldr += RubyNode(content, StringNode(prev))
    } else {
      bldr += StringNode(prev.substring(0, i + 1))
      bldr += RubyNode(content, StringNode(prev.substring(i + 1, prev.length)))
    }
  }

  def handleSystem(buffer: ListBuffer[Node]): Unit = {
    val prev = content
    if (inp.skipMatch("［＃「")) {
      var i = 0
      while (inp.peek != '」') {
        i += 1
        inp.next
      }
      if (inp.skipMatch("」に傍点］")) {
        val end = prev.length - i
        if (end > 0) {
          val text = prev.substring(0, end)
          val hled = prev.substring(text.length)
          buffer += StringNode(text)
          buffer += HighlightNode(StringNode(hled))
        }
      } else {
        inp.skipUntil('］', 50) //some mistype
      }
    } else {
      inp.skipUntil('］', 150)
      buf.append(prev) //restore buffer
    }
  }

  def handleSep(buffer: ListBuffer[Node]) = {
    while (AozoraParser.SENTENCE_SEPARATORS.contains(inp.peek.toChar)) {
      buf.append(inp.next.toChar)
    }
    //buffer += StringNode(content)
  }

  def parseSentence(): Option[HighLvlNode] = {
    val bldr = new ListBuffer[Node]

    @tailrec
    def rec(in: Int): Unit = {
      if (in == -1) return
      in.toChar match {
        case '｜'                                               => bldr += StringNode(content)
        case '《'                                               => handleRuby(bldr)
        case '［'                                               => handleSystem(bldr); return
        case '\n' | '\r'                                       => return
        case c if AozoraParser.SENTENCE_SEPARATORS.contains(c) => handleSep(bldr); return
        case c                                                 => buf.append(c)
      }
      inp.next
      rec(inp.peek)
    }

    rec(inp.peek)

    if (bldr.length == 0) {
      Some(Sentence(StringNode(content)))
    } else {
      bldr += StringNode(content)
      Some(Sentence(ListNode(bldr.toList.filter {
        case StringNode("") => false
        case _              => true
      })))
    }
  }

  private def calculateNextNode(): Option[HighLvlNode] = {
    val nn = inp.peek
    if (nn == -1) {
      return None
    }
    nn.toChar match {
      case '<'         => parseImgTag()
      case '\n' | '\r' => parseEndline()
      case '［'         => parseSystem()
      case _           => parseSentence()
    }
  }

  private var nextNode: Option[HighLvlNode] = calculateNextNode()

  def hasNext = !nextNode.isEmpty
  def next() = {
    val node = nextNode.get
    nextNode = calculateNextNode()
    node
  }
  def head = nextNode.get
}
