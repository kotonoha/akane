package ws.kotonoha.akane.parser

import scalax.file.Path
import scalax.io.Codec

/**
 * @author eiennohito
 * @since 15/08/10
 */
object KnpTabParserBench {
  def main(args: Array[String]): Unit = {
    val file = Path.fromString(args(0))
    val lines = file.lines()(Codec.UTF8).toArray

    val eoss = lines.zipWithIndex.collect {
      case ("EOS", i) => i
    }


    var prev = 0

    var lexCnt = 0L

    var i = 0
    val end = eoss.length

    val pars = new KnpTabFormatParser

    var j = 1

    val times = new Array[Long](20)
    times(0) = System.nanoTime()

    while (j < times.length) {
      val t = System.currentTimeMillis() % 10
      while (i < end) {
        val send = eoss(i)

        val res = pars.parse(lines.slice(prev, send))
        lexCnt += res.lexemes.length

        prev = send
        i += 1
      }
      times(j) = System.nanoTime()
      j += 1
      i = 0
    }

    while (i < times.length - 1) {
      val s = times(i)
      val e = times(i + 1)
      val d = e - s
      val milli = d * 1e-6
      println(f"$i: $milli%.3f millis")
      i += 1
    }

    println(s"$lexCnt")
  }
}
