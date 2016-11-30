package ws.kotonoha.akane.juman

import java.io.{BufferedReader, InputStream, InputStreamReader}

import ws.kotonoha.akane.analyzers.juman.{JumanPos, JumanStylePos}
import ws.kotonoha.akane.io.Charsets
import ws.kotonoha.akane.parser.JumanPosSet
import ws.kotonoha.akane.resources.Classpath

import scala.collection.mutable.ArrayBuffer

/**
  * @author eiennohito
  * @since 2016/11/24
  */
class PosCodec(data: Array[JumanPos]) {
  private val reverse: Map[Int, Int] = {
    data.zipWithIndex.map {
      case (o, i) => PosCodec.encodeRaw(o) -> (i + 1)
    }.toMap
  }

  def encode(pos: JumanStylePos): Int = {
    val rawCode = PosCodec.encodeRaw(pos)
    reverse.get(rawCode) match {
      case Some(code) => code
      case None => rawCode
    }
  }

  def decode(coded: Int): JumanPos = {
    if (coded > 0 && coded <= data.length) data(coded - 1) else PosCodec.decodeRaw(coded)
  }
}

object PosCodec {
  lazy val default: PosCodec = {
    Classpath.inputStream("/juman/pos.freq").map {
      fromStream
    }.obj
  }


  private val posset = JumanPosSet.default

  def parsePosEntry(line: String, start: Int, end: Int): JumanPos = {
    val parts = line.substring(start, end).split("-")
    if (parts.length != 4) {
      throw new Exception(s"$line was invalid pos tag")
    }

    val posString = parts(0)
    val pos = posset.pos.find(_.name == posString).getOrElse(posset.pos.head)

    val subString = parts(1)
    val subpos = pos.subtypes.find(_.name == subString).getOrElse(pos.subtypes.head)

    val conjString = parts(2)
    val conj = posset.conjugatons.find(_.name == conjString).getOrElse(posset.conjugatons.head)

    val cformString = parts(3)
    val cform = conj.conjugations.find(_.name == cformString).getOrElse(conj.conjugations.head)

    JumanPos(pos.num, subpos.num, conj.num, cform.num)
  }

  def fromStream(s: InputStream): PosCodec = {
    val reader = new BufferedReader(new InputStreamReader(s, Charsets.utf8))
    val result = new ArrayBuffer[JumanPos]()
    var line: String = null
    while ( {
      line = reader.readLine()
      line != null
    }) {
      val space = line.indexOf(' ')
      if (space == -1) {
        result += parsePosEntry(line, 0, line.length)
      } else {
        result += parsePosEntry(line, 0, space)
      }
    }

    new PosCodec(result.toArray)
  }

  def encodeRaw(pos: JumanStylePos): Int = {
    val coded =
      (pos.pos << 24) |
        (pos.subpos << 16) |
        (pos.category << 8) |
        pos.conjugation

    ~coded
  }

  def decodeRaw(rawCoded: Int): JumanPos = {
    val coded = ~rawCoded
    val pos = (coded >>> 24) & 0xff
    val subpos = (coded >>> 16) & 0xff
    val category = (coded >>> 8) & 0xff
    val conjugation = (coded >>> 0) & 0xff
    JumanPos(pos, subpos, category, conjugation)
  }
}
