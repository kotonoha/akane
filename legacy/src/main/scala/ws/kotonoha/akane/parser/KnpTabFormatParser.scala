package ws.kotonoha.akane.parser

import java.io.BufferedReader

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.lang3.StringUtils
import ws.kotonoha.akane.analyzers.knp.raw._
import ws.kotonoha.akane.pipe.knp.KnpResultParser
import ws.kotonoha.akane.utils.{DelimetedIterator, XDouble, XInt}

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
          proc.kihonku += new KihonkuBuilder(proc.kihonku.size, depNum, depType,
            parseFeatures(features), proc.lexemes.size)
          proc.bunsetsu.last.addKihonku()
        case Some(Groups("*", XInt(depNum), depType, features)) => //bunsetsu begin with * in knp output
          proc.bunsetsu += new BunsetsuBuilder(proc.bunsetsu.size, depNum, depType,
            parseFeatures(features), proc.lexemes.size, proc.kihonku.size)
        case _ if line == "EOS" => //do nothing
        case None if !startRe.pattern.matcher(line).find() => //it's a morpheme
          val lexeme = OldAndUglyKnpLexeme.fromTabFormat(line)
          proc.lexemes += lexeme
          proc.kihonku.last.addLexeme()
          proc.bunsetsu.last.addLexeme()
        case None if line.charAt(0) == '#' =>
          proc.setInfo(line.subSequence(1, line.length()).toString)
        case _ =>
          logger.warn(s"[$line] is not supported knp output")
      }
    }
    proc.result
  }

  override type Result = Option[OldAngUglyKnpTable]

  override def parse(reader: BufferedReader): Option[OldAngUglyKnpTable] = {
    val iter = new DelimetedIterator(reader, "EOS")
    Some(this.parse(iter))
  }
}

case class KnpInfo(id: Int, version: String, date: String, score: Double)

/**
 * A builder for bunsetsu objects
 *
 * @param depNumber number of dependency
 * @param depType type of dependency
 * @param features features that are present in bunsetsu
 */
class BunsetsuBuilder(val myNumber: Int, val depNumber: Int, val depType: String,
                      val features: Array[String],
                      val lexemeStart: Int, val kihonkuStart: Int)  {
  def result(lexs: LexemeStorage, kihs: KihonkuStorage): OldAndUglyBunsetsu =
    OldAndUglyBunsetsu(lexs, kihs, myNumber, depNumber, depType, features, lexemeStart, lexemeCnt, kihonkuStart, kihonkuCnt)

  var lexemeCnt = 0
  def addLexeme() = lexemeCnt += 1

  var kihonkuCnt = 0
  def addKihonku() = kihonkuCnt += 1
}

class KihonkuBuilder(val myNumber: Int, val depNumber: Int, val depType: String,
                     val features: Array[String],
                     val lexemeStart: Int)  {
  def result(lexs: LexemeStorage): OldAndUglyKihonku =
    OldAndUglyKihonku(lexs, myNumber, depNumber, depType, features, lexemeStart, lexemeCnt)

  var lexemeCnt = 0
  def addLexeme() = lexemeCnt += 1
}


class KnpTabParseProcess {
  val lexemes = new ArrayBuffer[OldAndUglyKnpLexeme]()
  val bunsetsu = new ArrayBuffer[BunsetsuBuilder]()
  val kihonku = new ArrayBuffer[KihonkuBuilder]()

  var info: String = _
  def setInfo(info: String) = this.info = info

  def result = {
    val lexData = lexemes.toArray
    val lexSt = new ArrayLexemeStorage(lexData)
    val kiData = kihonku.map(_.result(lexSt)).toArray
    val kst = new ArrayKihonkuStorage(kiData)
    new OldAngUglyKnpTable(
      info,
      lexData,
      bunsetsu.map(_.result(lexSt, kst)).toArray,
      kiData
    )
  }
}
