package ws.kotonoha.akane.parser

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex.Groups
import ws.kotonoha.akane.utils.{XDouble, XInt}
import org.apache.commons.lang3.StringUtils
import com.typesafe.scalalogging.slf4j.Logging
import ws.kotonoha.akane.pipe.knp.{KnpResultParser, KnpNode, KnpLexeme}
import java.io.{BufferedReader, InputStreamReader}

/**
 * @author eiennohito
 * @since 2014-04-10
 */
class KnpTabFormatParser extends KnpResultParser with Logging {

  val initRe = """(\*|\+) (-?\d+)([DP]) (.*)""".r.anchored

  def parseFeatures(in: String): Array[String] = {
    in.split("><").map(StringUtils.strip(_, "<>"))
  }

  val startRe = """^\*|\+|\#""".r

  val infoRe = """# S-ID:(\d+) KNP:([^ ]+) DATE:([^ ]+) SCORE:([-\d\.]+)""".r

  def parse(lines: TraversableOnce[String]) = {

    val proc = new KnpTabParseProcess
    for (line <- lines) {
      initRe.findPrefixMatchOf(line) match {
        case Some(Groups("+", XInt(depNum), depType, features)) => //kihonku begin with + in knp output
          proc.kihonku += new TreeTableBuilder(proc.kihonku.size, depNum, depType.intern(), parseFeatures(features))
        case Some(Groups("*", XInt(depNum), depType, features)) => //bunsetsu begin with * in knp output
          proc.bunsetsu += new TreeTableBuilder(proc.bunsetsu.size, depNum, depType.intern(), parseFeatures(features))
        case _ if line == "EOS" => //do nothing
        case None if !startRe.pattern.matcher(line).find() => //it's a morpheme
          val lexeme = KnpLexeme.fromTabFormat(line)
          proc.lexemes += lexeme
          proc.kihonku.last.addLexeme(lexeme)
          proc.bunsetsu.last.addLexeme(lexeme)
        case None if line.startsWith("#") =>
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

  override def hasNext = string == null || "EOS" == string || !reader.ready()
}

case class KnpInfo(id: Int, version: String, date: String, score: Double)

/**
 * A builder for bunsetsu objects
 * @param depNumber number of dependency
 * @param depType type of dependency
 * @param features features that are present in bunsetsu
 */
class TreeTableBuilder(val myNumber: Int, val depNumber: Int, val depType: String, val features: Array[String])  {
  def result: TableUnit = TableUnit(myNumber, depNumber, depType, features, lexemes.toArray)

  val lexemes = new ArrayBuffer[KnpLexeme]()

  def addLexeme(lexeme: KnpLexeme) = lexemes += lexeme
}


class KnpTabParseProcess {
  val lexemes = new ArrayBuffer[KnpLexeme]()
  val bunsetsu = new ArrayBuffer[TreeTableBuilder]()
  val kihonku = new ArrayBuffer[TreeTableBuilder]()

  var info: KnpInfo = _
  def setInfo(info: KnpInfo) = this.info = info

  def result = {
    new KnpTable(
      info,
      lexemes.toArray,
      bunsetsu.map(_.result).toArray,
      kihonku.map(_.result).toArray
    )
  }
}

/**
 * A unit of knp table entry that represents either a bunsetsu
 * dependency tree or a kihonku dependency tree
 * @param number number of entry
 * @param depNumber number of direct dependency
 * @param depType type of dependency
 * @param features features that are assigned to table entry
 * @param lexemes list of lexemes that compose this entry
 */
case class TableUnit(number: Int, depNumber: Int, depType: String, features: Array[String], lexemes: Array[KnpLexeme]) {
  def toNode = KnpNode(number, depType, lexemes.toList, features.toList, Nil)
}

case class KnpTable(info: KnpInfo, lexemes: Array[KnpLexeme], bunsetsu: Array[TableUnit], kihonku: Array[TableUnit]) {

  private def makeNode(unit: TableUnit, units: Traversable[TableUnit]): KnpNode = {
    val node = unit.toNode
    val children = units.filter(_.depNumber == unit.number)
    node.copy(children = children.map(n => makeNode(n, units)).toList)
  }

  def bunsetsuTree: KnpNode = {
    val root = bunsetsu.find(_.depNumber == -1).getOrElse(throw new NullPointerException("There is no root node in tree!"))
    makeNode(root, bunsetsu)
  }
}
