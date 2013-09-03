package ws.kotonoha.akane.knp

import java.io._
import java.util
import scala.collection.mutable.ListBuffer
import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers
import scalax.file.Path
import java.util.concurrent.atomic.AtomicInteger
import java.lang.ProcessBuilder.Redirect
import scala.concurrent.{Await, ExecutionContext}
import java.util.concurrent.TimeUnit
import ws.kotonoha.akane.config.KnpConfig
import ws.kotonoha.akane.pipe.{NamedPipes, AbstractRetryExecutor, Analyzer}
import com.typesafe.config.{ConfigFactory, Config}
import ws.kotonoha.akane.pipe.knp.{KnpPipeExecutorFactory, KnpPipeAnalyzer}

/**
 * @author eiennohito
 * @since 2013-08-12
 */
class KnpParser {

}










class KnpExecutorText extends FreeSpec with ShouldMatchers {
  "knp executor" - {
    "works" in {
      val knp = KnpPipeParser()
      val lines = knp.parse("私は何も知りません")
      lines.foreach(println)
      knp.close()
    }
  }
}


//かわったり かわったり かわる 動詞 2 * 0 子音動詞ラ行 10 タ系連用タリ形 15 "代表表記:代わる/かわる 自他動詞:他:代える/かえる"
case class KnpLexeme(
surface: String,
reading: String,
dicForm: String,
pos: String,
fld1: Int,
fld2: Option[String],
fld3: Int,
posType: String,
fld4: Int,
form: String,
fld5: Int,
info: String,
tags: List[String]
                      )

case class KnpItem(num: Int, star: String, plus: String, lexems: Seq[KnpLexeme])

object KnpParser {
  def parseTab(lines: Seq[String]) = {
    val buffer = new collection.mutable.ArrayBuffer[KnpItem]()
  }
}