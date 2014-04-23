package ws.kotonoha.akane.pipe.knp

import scala.collection.mutable.ArrayBuffer
import scala.util.matching.Regex.Groups
import ws.kotonoha.akane.utils.XInt

/**
 * @author eiennohito
 * @since 2014-04-10
 */
class KnpTreeParser {

  val initRe = """(\*|\+) (-?\d+)([DP]) (.*)""".r.anchored
  def parse(lines: TraversableOnce[String]) = {

    val proc = new KnpTreeParseProcess
    var lexeme = KnpLexeme
    for (line <- lines) {
      initRe.findPrefixMatchOf(line) match {
        case Some(Groups("+", XInt(depNum), depType, features)) => //kihonku begin with + in knp output
        case Some(Groups("*", XInt(depNum), depType, features)) => //bunsetsu begin with * in knp output
        case _ => //it's a morpheme
          if (!line.startsWith("#"))
            {
              val lexeme = KnpLexeme.fromTabFormat(line)
              proc.lexemes += lexeme
              proc.kihonku.last.addLexeme(lexeme)
              proc.bunsetsu.last.addLexeme(lexeme)
            }
          else {
            //skip comments
          }
      }
    }
  }
}

trait LexemeList {

}

class Bunsetsu
class Kihonku

trait LexemeListBuilder {
  val lexemes = new ArrayBuffer[KnpLexeme]()

  def addLexeme(lexeme: KnpLexeme) = lexemes += lexeme
}

class BunsetsuBuilder extends LexemeListBuilder
class KihonkuBuilder extends LexemeListBuilder

class KnpTreeParseProcess {
  val lexemes = new ArrayBuffer[KnpLexeme]()
  val bunsetsu = new ArrayBuffer[BunsetsuBuilder]()
  val kihonku = new ArrayBuffer[KihonkuBuilder]()
}

case class KnpTable() {
  def tree: KnpNode = ???
}
