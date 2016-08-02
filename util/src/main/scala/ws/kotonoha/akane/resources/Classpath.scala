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

package ws.kotonoha.akane.resources

import java.io.InputStream
import java.nio.charset.Charset

import org.apache.commons.io.IOUtils
import ws.kotonoha.akane.io.Charsets
import ws.kotonoha.akane.resources.FSPaths.AutoClosableWrapper

/**
  * @author eiennohito
  * @since 2016/07/12
  */
object Classpath {
  import scala.collection.JavaConverters._

  def lines(name: String, charset: Charset = Charsets.utf8): Seq[String] = {
    val proc = for (in <- inputStream(name)) yield {
      IOUtils.readLines(in, charset)
    }
    proc.obj.asScala
  }

  private def resourceUri(name: String) = {
    val fromClass = getClass.getResource(name)
    if (fromClass == null) {
      getClass.getClassLoader.getResource(name)
    } else fromClass
  }

  def inputStream(name: String): AutoClosableWrapper[InputStream] = {
    val obj = resourceUri(name)
    if (obj != null) {
      new AutoClosableWrapper[InputStream](obj.openStream())
    } else throw new Exception(s"resource $name was not found")
  }

  def fileAsString(name: String, charset: Charset = Charsets.utf8): String = {
    val url = resourceUri(name)
    if (url == null) {
      throw new Exception(s"resource $name was not found")
    }
    IOUtils.toString(url, charset)
  }
}
