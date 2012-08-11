package ru.dijkstra.ranobe_parser

import java.io.InputStream
import java.util

object State extends Enumeration {
  val ReadingKanjiExtent = Value("KANJI")
  val ReadingKanaExtent = Value("KANA")
  val ReadingHtmlTag = Value("HTML")
  val ReadingRubyExtent = Value("RUBY")
  val ReadingRomajiExtent = Value("ROMAJI")
  val ReadingPunctuation = Value("PUNCT")
}

class RanobeStructure {
  import java.util.ArrayList
  private var structure : ArrayList[Node] = new ArrayList[Node]()

  private val NEWLINE_SYMBOL : Int = '\n'
  private val RUBY_TAG_START : Int = '《'
  private val RUBY_TAG_END : Int = '》'
  private val KANJI_BREAKER : Int = '｜'
  private val QUOT_START: Int = '「'
  private val QUOT_END: Int = '」'
  private val DQUOT_START: Int = '『'
  private val DQUOT_END: Int = '』'
  private val JAP_DOT: Int = '。'
  private val JAP_QUEST: Int = '？'
  private val JAP_EXCL: Int = '！'

  private val SERVICE_EXTENT_START = "［＃"
  private val SERVICE_EXTENT_END = "］"
  private val NEXT_PAGE1 = "改"
  private val NEXT_PAGE2 = "ページ"
  private val BAKUTEN = "に傍点"


  private var readingServiceExtent = false
  private var state = State.ReadingPunctuation
  var buff : StringBuilder = new StringBuilder(50)
  private def flushBuffer {
    val str = buff.toString()
    state match {
        // По-человечески надо делать лист тьюплов (предикат => тип, определяемый предикатом)
        // Но я не знаю, как
      case State.ReadingHtmlTag => structure.add(new HtmlTagNode(str))
      case State.ReadingKanaExtent => structure.add(new KanaNode(str))
      case State.ReadingKanjiExtent => structure.add(new KanjiNode(str))
      case State.ReadingRomajiExtent => structure.add(new RomajiNode(str))
      case State.ReadingRubyExtent => structure.add(new RubyNode(str))
      case _ => {
        if (str.equals(SERVICE_EXTENT_START)) {
          if (readingServiceExtent) throw new Exception("Error: Nested Service extent")
          structure.add(ServiceNodeStart)
          readingServiceExtent = true
          discardBuffer()
          return
        }
        if (str.equals(SERVICE_EXTENT_END)) {
          if (!readingServiceExtent) {
            structure.add(new UnidentifiedPunctiation(SERVICE_EXTENT_END))
            discardBuffer()
            return
          }
          structure.add(ServiceNodeEnd)
          readingServiceExtent = false
          discardBuffer()
          return
        }
        structure.add(new UnidentifiedPunctiation(str))
      }
    }
    discardBuffer()
  }

  private def discardBuffer() {
    buff.clear()
  }

  def parseFromStream(in: InputStream) {
    import org.eiennohito.stolen_utils.UnicodeUtil._
    state = State.ReadingPunctuation
    while (in.available() > 0) {

    }
  }
}
