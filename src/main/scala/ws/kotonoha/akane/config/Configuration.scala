package ws.kotonoha.akane.config

import java.net.InetAddress
import com.typesafe.config.ConfigFactory

/**
 * @author eiennohito
 * @since 2013-09-02
 */
object Configuration {
  def withHostname(name: String): List[String] = {
    val localhost = InetAddress.getLocalHost.getHostName
    val dots = localhost.split("\\.").toList
    dots.scanLeft(List(name)) {
      case (l, part) => part :: l
    }.map(_.mkString("."))
  }

  def withUsername(name: String): List[String] = {
    val uname = System.getProperty("user.name")
    s"$uname.$name" :: Nil
  }

  def possibleNamesFor(name: String): List[String] = {
    val c1 = name :: withHostname(name) ::: withUsername(name).flatMap(withHostname)
    c1.map(_ + ".conf").distinct
  }

  def makeConfigFor(name: String) = {
    val names = possibleNamesFor(name)
    names.foldLeft(ConfigFactory.defaultOverrides()) {
      case (c, cname) => c.withFallback(ConfigFactory.parseResources(cname))
    }
  }
}


object AkaneConfig {
  lazy val default = Configuration.makeConfigFor("akane")
}