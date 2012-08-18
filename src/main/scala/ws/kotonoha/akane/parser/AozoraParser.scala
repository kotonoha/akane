package ws.kotonoha.akane.parser

import ws.kotonoha.akane.ast._
import java.nio.CharBuffer
import collection.mutable.ListBuffer
import scala.Some
import ws.kotonoha.akane.ast.Image
import annotation.tailrec
import org.eiennohito.stolen_utils.UnicodeUtil
import collection.immutable.HashSet

/**
 * @author eiennohito
 * @since 16.08.12
 */

trait AozoraInput {
  def peek: Int //doesn't go forward
  def next: Int //goes forward

  def skipMatch(s: String) = {
    var i = 0
    val len = s.length
    while (i < len && s(i) == peek.toChar) {
      i += 1
      next
    }
    len == i
  }

  def skipUntil(c: Char) {
    while (c != peek) {
      next
    }
  }
}



object AozoraParser {
  val SENTENCE_SEPARATORS = HashSet('。', '？', '！', '?', '!', '」')
}

class AozoraParser(inp: AozoraInput) extends BufferedIterator[HighLvlNode] {
  private val buf = CharBuffer.allocate(16 * 1024) // there shouldn't be sentences larger than 16kbtytes, rly

  private def content = {
    val str = new String(buf.array(), 0, buf.position())
    buf.clear()
    str
  }

  def parseImgTag(): Option[HighLvlNode] = {
    if (inp.skipMatch("<img src=\"")) {
      while (inp.peek != '\"') {
        buf.append(inp.next.toChar)
      }
      inp.skipUntil('>')
      Some(Image(content))
    } else {
      inp.skipUntil('>')
      calculateNextNode()
    }
  }

  def parseEndline(): Option[HighLvlNode] = {
    (inp.next.toChar, inp.peek.toChar) match {
      case ('\n', '\r') => inp.next;  Some(EndLine)
      case ('\r', '\n') => inp.next; Some(EndLine)
      case ('\n', _) => Some(EndLine)
      case _ => calculateNextNode()
    }
  }

  def parseSystem(): Option[HighLvlNode] = {
    if (inp.skipMatch("［＃改ページ］")) {
      Some(PageBreak)
    } else {
      inp.skipUntil('］')
      calculateNextNode()
    }
  }

  // | symbol commits string node, so we just need to find last non-kanji in buffer, it will be a border of ruby
  def handleRuby(bldr: ListBuffer[Node]): Unit = {
    inp.next
    val prev = content
    while (inp.peek != '》') {
      buf.append(inp.next.toChar)
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

  def handleSystem(buffer: ListBuffer[Node]) = {
    val prev = content
    if (inp.skipMatch("［＃「")) {
      var i = 0
      while (inp.peek != '」') {
        i += 1
        inp.next
      }
      if (inp.skipMatch("」に傍点］")) {
        val text = prev.substring(0, prev.length - i)
        val hled = prev.substring(text.length)
        buffer += StringNode(text)
        buffer += HighlightNode(StringNode(hled))
      } else {
        inp.skipUntil('］') //some mistype
      }
    } else {
      inp.skipUntil('］')
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
        case '｜' => bldr += StringNode(content)
        case '《' => handleRuby(bldr)
        case '［' => handleSystem(bldr)
        case '\n' | '\r' => return
        case c if AozoraParser.SENTENCE_SEPARATORS.contains(c) => handleSep(bldr); return
        case c => buf.append(c)
      }
      inp.next
      rec(inp.peek)
    }

    rec(inp.peek)

    if (bldr.length == 0) {
      Some(Sentence(StringNode(content)))
    } else {
      bldr += StringNode(content)
      Some(Sentence(ListNode(bldr.toList)))
    }
  }

  private def calculateNextNode(): Option[HighLvlNode] = {
    val nn = inp.peek
    if (nn == -1) {
      return None
    }
    nn.toChar match {
      case '<' => parseImgTag()
      case '\n' | '\r' => parseEndline()
      case '［' => parseSystem()
      case _ => parseSentence()
    }
  }

  private var nextNode: Option[HighLvlNode] = calculateNextNode()


  def hasNext = !nextNode.isEmpty
  def next() =  {
    val node = nextNode.get
    nextNode = calculateNextNode()
    node
  }
  def head = nextNode.get
}
