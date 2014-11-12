package ws.kotonoha.akane.parser

import java.io.BufferedReader

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.lang3.StringUtils
import ws.kotonoha.akane.pipe.knp.{KnpLexeme, KnpNode, KnpResultParser}
import ws.kotonoha.akane.utils.{XDouble, XInt}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex.Groups

/**
 * @author eiennohito
 * @since 2014-04-10
 */
class KnpTabFormatParser extends KnpResultParser with StrictLogging {

  val initRe = """(\*|\+) (-?\d+)([A-Z]) (.*)""".r.anchored

  def parseFeatures(in: String): Array[String] = {
    in.split("><").map(StringUtils.strip(_, "<>"))
  }

  val startRe = """^(?:;;|\*|\+|\#)""".r

  val infoRe = """# S-ID:(\d+) KNP:([^ ]+) DATE:([^ ]+) SCORE:([-\d\.]+)""".r

  def parse(lines: TraversableOnce[CharSequence]) = {

    val proc = new KnpTabParseProcess
    for (line <- lines) {
      initRe.findPrefixMatchOf(line) match {
        case Some(Groups("+", XInt(depNum), depType, features)) => //kihonku begin with + in knp output
          proc.kihonku += new KihonkuBuilder(proc.kihonku.size, depNum, depType.intern(),
            parseFeatures(features), proc.lexemes.size)
          proc.bunsetsu.last.addKihonku()
        case Some(Groups("*", XInt(depNum), depType, features)) => //bunsetsu begin with * in knp output
          proc.bunsetsu += new BunsetsuBuilder(proc.bunsetsu.size, depNum, depType.intern(),
            parseFeatures(features), proc.lexemes.size, proc.kihonku.size)
        case _ if line == "EOS" => //do nothing
        case None if !startRe.pattern.matcher(line).find() => //it's a morpheme
          val lexeme = KnpLexeme.fromTabFormat(line)
          proc.lexemes += lexeme
          proc.kihonku.last.addLexeme()
          proc.bunsetsu.last.addLexeme()
        case None if line.charAt(0) == '#' =>
          line match {
            case infoRe(XInt(id), version, date, XDouble(score)) =>
              proc.setInfo(KnpInfo(id, version, date, score))
            case _ => //ignore comments
          }
        case _ =>
          logger.warn(s"[$line] is not supported knp output")
      }
    }
    proc.result
  }

  override type Result = Option[KnpTable]

  override def parse(reader: BufferedReader): Option[KnpTable] = {
    val iter = new KnpOutputIterator(reader)
    Some(this.parse(iter))
  }
}

class KnpOutputIterator (val reader: BufferedReader) extends BufferedIterator[String] {

  var string: String = reader.readLine()

  override def head = string

  override def next() = {
    val data = string
    string = reader.readLine()
    data
  }

  override def hasNext = reader.ready() && string != "EOS"
}

case class KnpInfo(id: Int, version: String, date: String, score: Double)

/**
 * A builder for bunsetsu objects
 * @param depNumber number of dependency
 * @param depType type of dependency
 * @param features features that are present in bunsetsu
 */
class BunsetsuBuilder(val myNumber: Int, val depNumber: Int, val depType: String,
                      val features: Array[String],
                      val lexemeStart: Int, val kihonkuStart: Int)  {
  def result(lexs: LexemeStorage, kihs: KihonkuStorage): Bunsetsu =
    Bunsetsu(lexs, kihs, myNumber, depNumber, depType, features, lexemeStart, lexemeCnt, kihonkuStart, kihonkuCnt)

  var lexemeCnt = 0
  def addLexeme() = lexemeCnt += 1

  var kihonkuCnt = 0
  def addKihonku() = kihonkuCnt += 1
}

class KihonkuBuilder(val myNumber: Int, val depNumber: Int, val depType: String,
                     val features: Array[String],
                     val lexemeStart: Int)  {
  def result(lexs: LexemeStorage): Kihonku =
    Kihonku(lexs, myNumber, depNumber, depType, features, lexemeStart, lexemeCnt)

  var lexemeCnt = 0
  def addLexeme() = lexemeCnt += 1
}


class KnpTabParseProcess {
  val lexemes = new ArrayBuffer[KnpLexeme]()
  val bunsetsu = new ArrayBuffer[BunsetsuBuilder]()
  val kihonku = new ArrayBuffer[KihonkuBuilder]()

  var info: KnpInfo = _
  def setInfo(info: KnpInfo) = this.info = info

  def result = {
    val lexData = lexemes.toArray
    val lexSt = new ArrayLexemeStorage(lexData)
    val kiData = kihonku.map(_.result(lexSt)).toArray
    val kst = new ArrayKihonkuStorage(kiData)
    new KnpTable(
      info,
      lexData,
      bunsetsu.map(_.result(lexSt, kst)).toArray,
      kiData
    )
  }
}

trait LexemeStorage {
  def lexeme(num: Int): KnpLexeme
  def lexemeCnt: Int
  def lexemes(from: Int, until: Int): IndexedSeq[KnpLexeme]
}

class ArrayLexemeStorage(data: Array[KnpLexeme]) extends LexemeStorage {
  override def lexeme(num: Int) = data(num)
  override def lexemes(from: Int, until: Int) = data.slice(from, until)
  override def lexemeCnt = data.length
}

trait KihonkuStorage {
  def kihonku(num: Int): Kihonku
  def kihonkuCnt: Int
}

class ArrayKihonkuStorage(data: Array[Kihonku]) extends KihonkuStorage {
  override def kihonku(num: Int) = data(num)
  override def kihonkuCnt = data.length
}

/**
 * A unit of knp table entry that represents either a bunsetsu
 * dependency tree or a kihonku dependency tree
 * @param number number of entry
 * @param depNumber number of direct dependency
 * @param depType type of dependency
 * @param features features that are assigned to table entry
 * @param lexs lexeme storage
 */
case class Bunsetsu(lexs: LexemeStorage, kihs: KihonkuStorage,
                    number: Int, depNumber: Int, depType: String, features: Array[String],
                    lexemeStart: Int, lexemeCnt: Int,
                    kihonkuStart: Int, kihonkuCnt: Int) extends LexemeHelper {

  def toNode = KnpNode(number, depType, lexemes.toList, features.toList, Nil)

  override def toString = {
    s"Bunsetsu($number,$depNumber,$depType,[${lexemes.map(_.surface).mkString}}])"
  }
}

case class Kihonku(lexs: LexemeStorage, number: Int, depNumber: Int, depType: String, features: Array[String],
                   lexemeStart: Int, lexemeCnt: Int) extends LexemeHelper {
  override def toString = {
    s"Bunsetsu($number,$depNumber,$depType,[${lexemes.map(_.surface).mkString}}])"
  }
}

trait LexemeAccess {
  def lexeme(idx: Int): KnpLexeme
  def lexemeStart: Int
  def lexemeEnd: Int
  def lexemeCnt: Int
}

trait LexemeHelper extends LexemeAccess {
  def lexs: LexemeStorage
  def lexemeStart: Int
  def lexemeCnt: Int
  def lexemeEnd = lexemeStart + lexemeCnt

  def lexemes = lexs.lexemes(lexemeStart, lexemeEnd)

  def lexeme(idx: Int) = {
    assert(idx >= lexemeStart)
    assert(idx < lexemeEnd)
    lexs.lexeme(idx)
  }
}

case class KnpTable(info: KnpInfo, lexemes: Array[KnpLexeme], bunsetsu: Array[Bunsetsu], kihonku: Array[Kihonku]) {

  private def makeNode(unit: Bunsetsu, units: Traversable[Bunsetsu]): KnpNode = {
    val node = unit.toNode
    val children = units.filter(_.depNumber == unit.number)
    node.copy(children = children.map(n => makeNode(n, units)).toList)
  }

  def bunsetsuTree: KnpNode = {
    val root = bunsetsu.find(_.depNumber == -1)
      .getOrElse(throw new NullPointerException("There is no root node in tree!"))
    makeNode(root, bunsetsu)
  }

  def toJson: JsonKnpTable = {
    def jsonizeB(units: Array[Bunsetsu]): Array[JsonTableUnit] = {
      units.map { u => JsonTableUnit(u.number, u.depNumber, u.depType, u.features, u.lexemeCnt, u.kihonkuCnt)  }
    }

    def jsonizeK(units: Array[Kihonku]): Array[JsonTableUnit] = {
      units.map { u => JsonTableUnit(u.number, u.depNumber, u.depType, u.features, u.lexemeCnt, 0)  }
    }

    JsonKnpTable(info, lexemes, jsonizeB(bunsetsu), jsonizeK(kihonku))
  }
}


case class JsonKnpTable(info: KnpInfo, lexemes: Array[KnpLexeme],
                        bunsetsu: Array[JsonTableUnit], kihonku: Array[JsonTableUnit]) {
  def toModel: KnpTable = {
    def normalizeK(lexs: LexemeStorage, units: Array[JsonTableUnit]) = {
      val bldr = new mutable.ArrayBuilder.ofRef[Kihonku]()
      var start = 0
      for (k <- units) {
        bldr += Kihonku(lexs, k.number, k.depNumber, k.depType, k.features, start, k.lexemes)
        start += k.lexemes
      }
      bldr.result()
    }
    def normalizeB(lexs: LexemeStorage, kis: KihonkuStorage, data: Array[JsonTableUnit]) = {
      val bldr = new mutable.ArrayBuilder.ofRef[Bunsetsu]()
      var lexStart = 0
      var kiStart = 0
      for (b <- data) {
        bldr += Bunsetsu(lexs, kis, b.number, b.depNumber, b.depType, b.features,
          lexStart, b.lexemes, kiStart, b.kihonku)
        lexStart += b.lexemes
        kiStart += b.kihonku
      }
      bldr.result()
    }
    val lexs = new ArrayLexemeStorage(lexemes)
    val kh = normalizeK(lexs, kihonku)
    val khs = new ArrayKihonkuStorage(kh)
    KnpTable(info, lexemes, normalizeB(lexs, khs, bunsetsu), kh)
  }
}
case class JsonTableUnit(number: Int, depNumber: Int, depType: String, features: Array[String],
                         lexemes: Int, kihonku: Int)
