package ru.dijkstra.ranobe_parser.ruby

import ru.dijkstra.ranobe_parser.ast.{ListNode, RubyNode, StringNode, Node}
import collection.mutable.ListBuffer

/**
 * @author eiennohito
 * @since 15.08.12
 */

object AfterLexical {

  def matchFront(wr: String, rd: String) = {
    var i = 0
    while (wr(i) == rd(i)) {
      i += 1
    }
    i
  }

  def matchBack(wr: String, rd: String) = {
    var i = 0
    val wl = wr.length - 1
    val rl = rd.length - 1
    val max = math.min(wl, rl)
    while (i < max && wr(wl - i) == rd(rl - i)) {
      i += 1
    }
    i
  }

  def matchMid(wr: String, rd: String): Node = {
    RubyNode(rd, StringNode(wr))
  }

  /**
   * Makes node from parsed values
   * @param writing - writing (as from lexical analyzer)
   * @param reading - reading (as from lexical analyzer)
   * @return
   */
  def makeNode(writing: String, reading: String): Node = {
    if (writing equals reading) {
      return StringNode(reading)
    }
    val nodes = ListBuffer[Node]()
    val fr = matchFront(writing, reading)
    if (fr != 0) {
      nodes += StringNode(reading.substring(0, fr))
    }
    val bck = matchBack(writing.substring(fr), reading.substring(fr))
    val wbl = writing.length - bck
    val rbl = reading.length - bck
    nodes += matchMid(writing.substring(fr, wbl), reading.substring(fr, rbl))
    if (bck != 0) {
      nodes += StringNode(reading.substring(rbl, reading.length))
    }
    ListNode(nodes.toList)
  }
}
