package ws.kotonoha.akane.tools

import scala.concurrent.forkjoin.ForkJoinPool
import scala.concurrent.{Await, Future, ExecutionContext}
import scalax.file.Path
import java.io.{InputStreamReader, Reader}
import ws.kotonoha.akane.parser.{AozoraParser, StreamReaderInput}
import ws.kotonoha.akane.juman.{JumanDaihyou, JumanPipeExecutor}
import ws.kotonoha.akane.render.MetaStringRenderer
import ws.kotonoha.akane.ast.Sentence
import com.typesafe.scalalogging.slf4j.Logging

/**
 * @author eiennohito
 * @since 27.03.13 
 */

case class Information(writing: String, reading: String, pos: String)

class LazyTheadLocal[T <: AnyRef](factory: => T) {
  private val value = new ThreadLocal[T]

  def get() = {
    if (value.get() == null) {
      value.set(factory)
    }
    value.get()
  }

  def clear() = value.remove()
}

object WordAggregator extends Logging {
  implicit val context = {
    val ex = new ForkJoinPool(8)
    ExecutionContext.fromExecutor(ex)
  }

  object juman extends LazyTheadLocal(makeJuman)


  def makeJuman: JumanPipeExecutor = {
    JumanPipeExecutor.apply()
  }

  def loadNs(path: Path, level: String) = {
    path.lines().grouped(3) map {
      case Seq(w, r, _) =>
        if (w == "") JumanDaihyou(r, "") -> level
        else JumanDaihyou(w, r) -> level
    } toMap
  }

  val nmap = {
    val base = Path.fromString("e:\\Temp\\wap_soft\\jlpt\\")
    val seq = Seq.range(5, 0, -1).map(i => loadNs(base / s"n$i.txt", s"N$i"))
    seq.toSeq.reduce(_ ++ _)
  }

  val ignorePos = Set("特殊", "助詞", "判定詞", "接尾辞", "指示詞", "助動詞", "接頭辞", "接続詞")

  def parse(in: InputStreamReader) = {
    val sri = new StreamReaderInput(in)
    val ap = new AozoraParser(sri)
    val juman = this.juman.get
    val rend = new MetaStringRenderer
    var words = 0
    val res = ap.foldLeft(Map[Information, Int]().withDefaultValue(0)) {
      case (m, Sentence(node)) =>
        val sent = rend.render(node)
        val entries = juman.parse(sent.data).filterNot(e => ignorePos.contains(e.spPart))
        entries.foldLeft(m) { case (m, je) =>
          val d = je.daihyou
          words += 1
          val i = Information(d.writing, d.reading, je.spPart)
          m.updated(i, m(i) + 1)
        }
      case (m, _) => m
    }
    (res, words)
  }

  def main(args: Array[String]) {
    val path = Path.fromString(args(0))
    val enc = args(1)
    logger.info(s"processing directory $path with encoding $enc")

    val ents = path.children().iterator.map {
      f =>
        Future.apply (
          f.inputStream().map { is =>
            val rd = new InputStreamReader(is, enc)
            val (map, words) = parse(rd)
            logger.info(s"Parsed file: ${f.name} -- ${words} words")
            map
          } either match {
            case Right(x) => Some(x)
            case Left(errs) =>
              errs.foreach { e =>
                logger.error(s"can't process ${f.name}", e)
              }
              None
          }
        )
    }

    import concurrent.duration._

    val extracted = Future.sequence(ents.toSeq).map(x => x.flatten)
    val res = Await.result(extracted, 1 hour)

    val data = res.reduce { (m1, m2) => m2.foldLeft(m1) {
      case (m, (k, cnt)) => m.updated(k, m(k) + cnt)
    }}

    val sorted = data.toSeq.sortBy(-_._2)

    val tout = sorted.map {
      case (nfo, cnt) =>
        val jd = JumanDaihyou(nfo.writing, nfo.reading)
        val item = nmap.get(jd).getOrElse("")
        Seq(nfo.pos, nfo.writing, nfo.reading, item, cnt).foldLeft(new StringBuilder(512)) {
          (sb, i) =>
            sb.append("\"")
            sb.append(i)
            sb.append("\"\t")
        }.result()
    }

    val outf = Path.fromString("e:/Temp/word_data.csv")
    outf.writeStrings(tout, "\n")
  }
}
