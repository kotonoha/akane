/*
 * Copyright 2012-2016 eiennohito (Tolmachev Arseny)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
