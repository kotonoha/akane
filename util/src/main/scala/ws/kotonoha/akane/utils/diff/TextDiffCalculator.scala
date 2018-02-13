package ws.kotonoha.akane.utils.diff

import ws.kotonoha.akane.utils.IntBuffer

import scala.annotation.tailrec
import scala.collection.mutable.ArrayBuffer

/**
  * @author eiennohito
  * @since 2016/09/12
  */
final class TextDiffCfg(data: Array[Int]) {
  if (data.length != 16) {
    throw new Exception("length should be 16")
  }

  def apply(last: Int, cur: Int): Int = {
    data(last * 4 + cur)
  }
}

object TextDiffCfg {
  def apply(
      replaceCost: Int = 1,
      insertCost: Int = 1,
      deleteCost: Int = 1,
      insertInsert: Int = 1,
      insertDelete: Int = 1,
      insertReplace: Int = 1,
      deleteInsert: Int = 1,
      deleteDelete: Int = 1,
      deleteReplace: Int = 1,
      replaceInsert: Int = 1,
      replaceDelete: Int = 1,
      replaceReplace: Int = 1
  ): TextDiffCfg = {
    val data = Array[Int](
      0,
      insertCost,
      deleteCost,
      replaceCost,
      0,
      insertInsert,
      insertDelete,
      insertReplace,
      0,
      deleteInsert,
      deleteDelete,
      deleteReplace,
      0,
      replaceInsert,
      replaceDelete,
      replaceReplace
    )
    new TextDiffCfg(data)
  }

  val default = TextDiffCfg()
}

class TextDiffCalculator(cfg: TextDiffCfg = TextDiffCfg.default) {
  private[this] val buffer = new IntBuffer()
  private[this] var xlen = 0
  private[this] var ylen = 0
  private[this] var xsize = 0
  private[this] var lastStep = DiffTypes.TRANSITION_EQUALS

  private def checkCoord(x: Int, y: Int) = {
    if (x > xlen || y > ylen || x < 0 || y < 0) {
      throw new IndexOutOfBoundsException(
        s"coordinate was invalid: [$x, $y], can be [$xlen, $ylen]")
    }
  }

  private def mat(x: Int, y: Int): Int = {
    checkCoord(x, y)
    val crd = y * xsize + x
    buffer(crd)
  }

  private def matSet(x: Int, y: Int, v: Int): Unit = {
    checkCoord(x, y)
    val crd = y * xsize + x
    buffer(crd) = v
  }

  private def init(s1: CharSequence, s2: CharSequence) = {
    xlen = s1.length()
    ylen = s2.length()
    xsize = xlen + 1
    buffer.ensureSize((xlen + 1) * (ylen + 1))
    var i = 1
    while (i < (xlen + 1)) {
      val cost = if (i == 1) {
        cfg(DiffTypes.TRANSITION_EQUALS, DiffTypes.TRANSITION_DELETE)
      } else {
        cfg(DiffTypes.TRANSITION_DELETE, DiffTypes.TRANSITION_DELETE)
      }
      val ncost = mat(i - 1, 0) + cost
      matSet(i, 0, ncost | DiffTypes.TRANSITION_DELETE << 24)
      i += 1
    }
    i = 1
    while (i < (ylen + 1)) {
      val cost = if (i == 1) {
        cfg(DiffTypes.TRANSITION_EQUALS, DiffTypes.TRANSITION_INSERT)
      } else {
        cfg(DiffTypes.TRANSITION_INSERT, DiffTypes.TRANSITION_INSERT)
      }
      val ncost = mat(0, i - 1) + cost
      matSet(0, i, ncost | DiffTypes.TRANSITION_INSERT << 24)
      i += 1
    }
  }

  private[this] def scoreFor(i: Int, j: Int) = {
    rawScoreFor(i, j) & 0x00ffffff
  }

  private[this] def rawScoreFor(i: Int, j: Int) = {
    mat(i + 1, j + 1)
  }

  private[this] def nonMatchScoreFor(i: Int, j: Int) = {
    val raws1 = rawScoreFor(i, j - 1)
    val raws2 = rawScoreFor(i - 1, j)
    val raws3 = rawScoreFor(i - 1, j - 1)

    val rs1 = raws1 & 0xffffff
    val rs2 = raws2 & 0xffffff
    val rs3 = raws3 & 0xffffff

    val step1 = raws1 >>> 24
    val step2 = raws2 >>> 24
    val step3 = raws3 >>> 24

    val s1 = rs1 + cfg(step1, DiffTypes.TRANSITION_INSERT)
    val s2 = rs2 + cfg(step2, DiffTypes.TRANSITION_DELETE)
    val s3 = rs3 + cfg(step3, DiffTypes.TRANSITION_REPLACE)
    var result = s3
    var resultStep = DiffTypes.TRANSITION_REPLACE
    if (s2 < result) {
      result = s2
      resultStep = DiffTypes.TRANSITION_DELETE
    }
    if (s1 < result) {
      result = s1
      resultStep = DiffTypes.TRANSITION_INSERT
    }
    lastStep = resultStep
    result
  }

  private def setScore(i: Int, j: Int, score: Int, kind: Int) = {
    matSet(i + 1, j + 1, score | kind << 24)
  }

  private def forward(s1: CharSequence, s2: CharSequence): Unit = {
    var i = 0
    while (i < xlen) {
      val cx = s1.charAt(i)

      var j = 0
      while (j < ylen) {
        val cy = s2.charAt(j)

        val score = cy match {
          case `cx` =>
            lastStep = DiffTypes.TRANSITION_EQUALS
            scoreFor(i - 1, j - 1)
          case _ =>
            nonMatchScoreFor(i, j)
        }
        setScore(i, j, score, lastStep)

        j += 1
      }
      i += 1
    }
  }

  def printMatrix(s1: CharSequence, s2: CharSequence): Unit = {
    val bldr = new StringBuilder
    bldr.append("      ")

    var j = 0
    while (j < ylen) {
      bldr.append(" ")
      bldr.append(s2.charAt(j))
      bldr.append(" ")
      j += 1
    }
    bldr.append("\n")
    var i = 0
    while (i < (xlen + 1)) {

      if (i > 0) {
        bldr.append(" ")
        bldr.append(s1.charAt(i - 1))
        bldr.append(" ")
      } else {
        bldr.append("   ")
      }

      var j = 0
      while (j < (ylen + 1)) {
        val matval = mat(i, j)
        val cost = matval & 0xffffff
        val tp = matval >> 24
        val tpchar = tp match {
          case DiffTypes.TRANSITION_EQUALS  => 'e'
          case DiffTypes.TRANSITION_INSERT  => 'i'
          case DiffTypes.TRANSITION_DELETE  => 'd'
          case DiffTypes.TRANSITION_REPLACE => 'r'
        }
        bldr.append(f"$cost%2d$tpchar")
        j += 1
      }

      bldr.append("\n")
      i += 1
    }

    println(bldr.result())
  }

  private def collect(s1: CharSequence, s2: CharSequence): TextDiff = {
    val bldr = new DiffBuilder
    bldr.compute(s1, s2)
  }

  def calculate(s1: CharSequence, s2: CharSequence): TextDiff = {
    init(s1, s2)
    forward(s1, s2)
    collect(s1, s2)
  }

  def score(s1: CharSequence, s2: CharSequence): Int = {
    init(s1, s2)
    forward(s1, s2)
    scoreFor(xlen - 1, ylen - 1)
  }

  class DiffBuilder {
    import DiffTypes._

    private val spans = new ArrayBuffer[DiffSpan]
    private var curType = -1
    private var curEndX = -1
    private var curEndY = -1

    private def push(x: Int, y: Int) = {
      if (x != curEndX || y != curEndY) {
        spans += DiffSpan(curType, x, curEndX, y, curEndY)
      }
    }

    private def addEqual(x: Int, y: Int): Unit = {
      curType match {
        case TRANSITION_EQUALS =>
        case -1 =>
          curType = TRANSITION_EQUALS
        case _ =>
          push(x + 1, y + 1)
          curType = TRANSITION_EQUALS
          curEndX = x + 1
          curEndY = y + 1
      }
    }

    private def addReplace(x: Int, y: Int): Unit = {
      curType match {
        case TRANSITION_REPLACE =>
        case TRANSITION_INSERT | TRANSITION_DELETE =>
          curType = TRANSITION_REPLACE
        case -1 =>
          curType = TRANSITION_REPLACE
        case _ =>
          push(x + 1, y + 1)
          curType = TRANSITION_REPLACE
          curEndX = x + 1
          curEndY = y + 1
      }
    }

    private def addAdd(x: Int, y: Int) = {
      curType match {
        case TRANSITION_INSERT | TRANSITION_REPLACE =>
        case TRANSITION_DELETE =>
          curType = TRANSITION_REPLACE
        case -1 =>
          curType = TRANSITION_INSERT
        case _ =>
          push(x + 1, y + 1)
          curType = TRANSITION_INSERT
          curEndX = x + 1
          curEndY = y + 1
      }
    }

    private def addDelete(x: Int, y: Int) = {
      curType match {
        case TRANSITION_DELETE | TRANSITION_REPLACE =>
        case TRANSITION_INSERT =>
          curType = TRANSITION_REPLACE
        case -1 =>
          curType = TRANSITION_DELETE
        case _ =>
          push(x + 1, y + 1)
          curType = TRANSITION_DELETE
          curEndX = x + 1
          curEndY = y + 1
      }
    }

    @tailrec
    private def computeSpans(inx: Int, iny: Int): Unit = {
      val x = inx.max(-1)
      val y = iny.max(-1)
      if (x == -1 || y == -1) {
        if (x == -1 && y == -1) return
        if (x == -1) {
          addAdd(-1, y)
        } else if (y == -1) {
          addDelete(x, -1)
        }
        return
      }

      val score = rawScoreFor(x, y)
      val step = score >>> 24
      step match {
        case DiffTypes.TRANSITION_EQUALS =>
          addEqual(x, y)
          computeSpans(x - 1, y - 1)
        case DiffTypes.TRANSITION_INSERT =>
          addAdd(x, y)
          computeSpans(x, y - 1)
        case DiffTypes.TRANSITION_DELETE =>
          addDelete(x, y)
          computeSpans(x - 1, y)
        case DiffTypes.TRANSITION_REPLACE =>
          addReplace(x, y)
          computeSpans(x - 1, y - 1)
      }
    }

    def compute(s1: CharSequence, s2: CharSequence): TextDiff = {
      curEndX = s1.length()
      curEndY = s2.length()
      val score = scoreFor(xlen - 1, ylen - 1)
      computeSpans(xlen - 1, ylen - 1)
      push(0, 0)
      TextDiff(score, spans.reverse)
    }
  }
}

case class DiffSpan(kind: Int, xs: Int, xe: Int, ys: Int, ye: Int)
case class TextDiff(score: Int, spans: Seq[DiffSpan])

class SeqFirstEndBorders(seq: TextDiff) extends SegmentBorders {
  override def length: Int = seq.spans.length
  override def apply(pos: Int): Int = seq.spans(pos).xe
}
