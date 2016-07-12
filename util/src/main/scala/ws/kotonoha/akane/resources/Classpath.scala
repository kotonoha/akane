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
  def resource(name: String): AutoClosableWrapper[InputStream] = {
    val obj = this.getClass.getClassLoader.getResource(name)
    if (obj != null) {
      new AutoClosableWrapper[InputStream](obj.openStream())
    } else throw new Exception(s"resource $name was not found")
  }

  def fileAsString(name: String, charset: Charset = Charsets.utf8): String = {
    val url = this.getClass.getClassLoader.getResource(name)
    if (url == null) {
      throw new Exception(s"resource $name was not found")
    }
    IOUtils.toString(url, charset)
  }
}
