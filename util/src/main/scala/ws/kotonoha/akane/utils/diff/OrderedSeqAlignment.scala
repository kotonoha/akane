package ws.kotonoha.akane.utils.diff

import ws.kotonoha.akane.utils.IntBuffer

import scala.collection.mutable.ArrayBuffer

/**
  * Segment borders of a segmented sentence.
  */
trait SegmentBorders {
  def length: Int
  def apply(pos: Int): Int
}

trait SegmentedSequence {
  def segmentation: SegmentBorders
  def appendSurface(from: Int, to: Int, sb: StringBuilder): Unit
  def length: Int
}

case class SegmentSpan(start: Int, end: Int, kind: Int, content: IntBuffer)
case class SegmentDiff(spans: Seq[SegmentSpan], inserts: Int, deletes: Int, replaces: Int) {
  def fullMatch: Boolean = {
    inserts == 0 && deletes == 0 && replaces == 0
  }

  def display(
    obj: SegmentedSequence,
    removed: String = "X",
    added: String = "+"
  ): String = {
    val sb = new StringBuilder
    val seq = obj.segmentation

    var outFinished = 0

    def displayDeletes(s: SegmentSpan): Unit = {
      var i = s.start
      val end = s.end
      while (i < end) {
        val pos = seq.apply(i)
        obj.appendSurface(outFinished, pos, sb)
        sb.append(removed)
        outFinished = pos
        i += 1
      }
    }

    def displayInserts(s: SegmentSpan): Unit = {
      var i = 0
      val end = s.content.length
      while (i < end) {
        val pos = s.content(i)
        obj.appendSurface(outFinished, pos, sb)
        sb.append(added)
        outFinished = pos
        i += 1
      }
    }

    def displayTo(pos: Int): Unit = {
      obj.appendSurface(outFinished, pos, sb)
      outFinished = pos
    }

    def displayNormal(s: SegmentSpan): Unit = {
      val last = s.end - 1
      val pos = seq(last)
      displayTo(pos)
    }

    spans.foreach { s =>
      s.kind match {
        case DiffTypes.TRANSITION_EQUALS =>
          displayNormal(s)
        case DiffTypes.TRANSITION_DELETE =>
          displayDeletes(s)
          displayNormal(s)
        case DiffTypes.TRANSITION_INSERT =>
          displayInserts(s)
        case DiffTypes.TRANSITION_REPLACE =>
          val savedOut = outFinished
          sb.append("[")
          displayDeletes(s)
          val pos = s.content(s.content.length - 1)
          if (pos > outFinished) {
            displayTo(pos)
          }
          sb.append("/")
          outFinished = savedOut
          displayInserts(s)
          sb.append("]")
      }
    }

    obj.appendSurface(outFinished, obj.length, sb)

    sb.toString()
  }
}

object SegmentDiff {
  def compute(s1: SegmentBorders, s2: SegmentBorders): SegmentDiff = {
    new SequenceAligner(s1, s2).align()
  }
}

class SequenceAligner(s1: SegmentBorders, s2: SegmentBorders) extends DiffTypes {

  import DiffTypes._

  private def l1 = s1.length
  private def l2 = s2.length

  private var lastState = TRANSITION_EQUALS
  private var stateStart = 0
  private var stateEnd = 0

  private var inserts = 0
  private var deletes = 0
  private var replaces = 0

  private val stateBuffer = new ArrayBuffer[SegmentSpan]()
  private var replaceElements = new IntBuffer()

  private def pushState() = {
    if (stateStart != stateEnd || replaceElements.length > 0) {
      stateBuffer += SegmentSpan(stateStart, stateEnd, lastState, if (replaceElements.length > 0) replaceElements else IntBuffer.empty)
      if (replaceElements.length > 0) {
        replaceElements = new IntBuffer()
      }
      stateStart = i
      stateEnd = i
    }
  }

  private def handleEquals() = {
    if (lastState == TRANSITION_EQUALS) {
      stateEnd += 1
    } else {
      pushState()
      lastState = TRANSITION_EQUALS
    }
    i += 1
    j += 1
    stateEnd = i
  }

  private var i = 0
  private var j = 0

  private def goNuts(c1: Int, c2: Int): Unit = {
    if (lastState == TRANSITION_EQUALS) {
      pushState()
      lastState = if (c1 > c2) {
        inserts += 1
        replaceElements.append(c2)
        TRANSITION_INSERT
      } else {
        deletes += 1
        TRANSITION_DELETE
      }
    } else if (lastState == TRANSITION_DELETE) {
      if (c1 > c2) {
        lastState = TRANSITION_REPLACE
        deletes -= 1
        replaces += 1
        replaceElements.append(c2)
      }
    } else if (lastState == TRANSITION_INSERT) {
      if (c2 > c1) {
        lastState = TRANSITION_REPLACE
        inserts -= 1
        replaces += 1
      } else {
        replaceElements.append(c2)
      }
    } else if (lastState == TRANSITION_REPLACE) {
      if (c1 > c2) {
        replaceElements.append(c2)
      }
    }

    if (c1 > c2) {
      j += 1
    } else {
      i += 1
      stateEnd += 1
    }
  }

  def align(): SegmentDiff = {
    while (i < l1 && j < l2) {
      val c1 = s1(i)
      val c2 = s2(j)

      if (c1 == c2) {
        handleEquals()
      } else {
        goNuts(c1, c2)
      }
    }

    handleEnd()
    SegmentDiff(stateBuffer, inserts, deletes, replaces)
  }

  private def handleEnd(): Unit = {
    if (lastState == TRANSITION_EQUALS) {
      pushState()
      if (i < l1) {
        deletes += 1
        lastState = TRANSITION_DELETE
        stateStart = i
        stateEnd = l1
      } else if (j < l2) {
        inserts += 1
        lastState = TRANSITION_INSERT
        stateStart = i
        stateEnd = i

        while (j < l2) {
          replaceElements.append(s2(j))
          j += 1
        }
      }
      pushState()
    } else {
      lastState match {
        case TRANSITION_INSERT =>
          inserts -= 1
          replaces += 1
        case TRANSITION_DELETE =>
          deletes -= 1
          replaces += 1
        case _ =>
      }
      lastState = TRANSITION_REPLACE
      if (i < l1) {
        stateEnd = i
      } else {
        while (j < l2) {
          replaceElements.append(s2(j))
          j += 1
        }
      }
      pushState()
    }
  }
}
