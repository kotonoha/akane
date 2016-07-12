package ws.kotonoha.akane.tools

import scalax.file.Path
import scalax.io.Codec
import java.io.{FileInputStream, InputStreamReader}
import java.nio.charset.Charset
import scala.collection.mutable
import ws.kotonoha.akane.unicode.UnicodeUtil
import ws.kotonoha.akane.dict.kanjidic2.Kanjidic2Parser
import ws.kotonoha.akane.kanji.Jouyou

/**
 * @author eiennohito
 * @since 25.03.13 
 */


//hint: Windows-31J
object KanjiAggregator {
  import ws.kotonoha.akane.dict.kanjidic2.KanjidicTypes._

  val approvecReadings = Set(kunyomi, onyomi)

  private final val out = Path.fromString("e:\\Temp\\kanji.csv")

  private final val kdictf = Path.fromString("e:\\Temp\\wap_soft\\kanjidic2.xml")

  def main(args: Array[String]) {
    val path = Path.fromString(args(0))
    val cs = Charset.forName(args(1))
    val items = new mutable.HashMap[Int, Int]().withDefaultValue(0)
    for (
      fl <- path.children();
      is <- fl.inputStream()
    ) {
      val reader = new InputStreamReader(is, cs)
      UnicodeUtil.stream(reader).filter(UnicodeUtil.isKanji).foreach(k => items(k) += 1)
    }
    val seq = items.toSeq.sortBy(-_._2)


    for (is <- kdictf.inputStream) {
      val kdict = Kanjidic2Parser.parse(is)

      val kanji = kdict.map(x => x.literal -> x).toMap

      val res = seq.map { case (cp, cnt) =>
          val lit = new String(Character.toChars(cp))
          val kt = Jouyou.category(lit).toString
          kanji.get(lit) match {
            case None => s"$lit\t$cnt\t$kt"
            case Some(ki) =>
              Seq(lit, cnt, kt,
                ki.rmgroups.flatMap(_.readings).filter(x => approvecReadings.contains(x.name)).map(_.value).mkString(", "),
                ki.rmgroups.flatMap(_.meanings).filter(x => x.loc.equals("en")).map(_.str).mkString(", "),
                ki.misc.jlpt.getOrElse(""),
                ki.misc.grade.map(_.toString).getOrElse("")
              ).map(x => "\"%s\"".format(x)).mkString("\t")
          }
      }

      out.writeStrings(res, "\n")(Codec.UTF8)
    }
  }
}
