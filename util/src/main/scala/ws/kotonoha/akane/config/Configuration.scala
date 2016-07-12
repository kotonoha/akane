package ws.kotonoha.akane.config

import java.net.InetAddress

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging

/**
 * @author eiennohito
 * @since 2013-09-02
 */
object Configuration extends StrictLogging {
  def withHostname(name: String): List[String] = {
    val localhost = InetAddress.getLocalHost.getHostName
    val dots: List[String] = localhost.split("\\.").toList

    (dots.scanLeft(List(name)) {
      case (l, part) => part :: l
    } ++ dots.scanRight(List[String](name)) {
      case (l, part) => l :: part
    }).map(_.mkString(".")).filter(_.length > 2).distinct
  }

  def withUsername(name: String, config: Config = ConfigFactory.defaultOverrides()): List[String] = {
    val uname = config.getString("user.name")
    s"$uname.$name" :: Nil
  }

  def possibleNamesFor(name: String, defaults: Config = ConfigFactory.defaultOverrides()): List[String] = {
    val c1 = name :: withHostname(name) ::: withUsername(name, defaults).flatMap(withHostname)
    c1.map(_ + ".conf").distinct
  }

  def makeConfigFor(name: String, defaults: Config = ConfigFactory.defaultOverrides()) = {
    val names = possibleNamesFor(name, defaults)
    logger.debug(s"For config {$name} trying [${names.mkString(", ")}]")
    names.foldLeft(defaults) {
      case (c, cname) =>
        val config = ConfigFactory.parseResources(cname)
        if (!config.isEmpty) {
          logger.debug(s"Loaded config from file $cname")
          config.withFallback(c)
        } else c
    }
  }
}
