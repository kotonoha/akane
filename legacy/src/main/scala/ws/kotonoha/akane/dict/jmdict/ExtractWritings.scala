package ws.kotonoha.akane.dict.jmdict

import java.io.FileInputStream
import java.nio.charset.Charset
import java.nio.file.{Files, Paths}

/**
  * @author eiennohito
  * @since 2015/12/09
  */
object ExtractWritings {
  import scala.collection.JavaConverters._

  def main(args: Array[String]): Unit = {
    val file = args(0)
    val out = args(1)

    val is = new FileInputStream(file)

    val parser = JMDictParser.parse(is)
    val outData = parser.flatMap(_.writing.map(_.value))

    val outPath = Paths.get(out)

    Files.createDirectories(outPath.getParent)
    Files.write(outPath, new java.lang.Iterable[String] {
      override def iterator() = outData.asJava
    }, Charset.forName("utf-8"))

    is.close()
  }
}
