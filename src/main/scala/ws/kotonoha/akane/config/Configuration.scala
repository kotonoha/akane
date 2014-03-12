package ws.kotonoha.akane.config

import java.net.InetAddress
import com.typesafe.config.{Config, ConfigFactory}

/**
 * @author eiennohito
 * @since 2013-09-02
 */
object Configuration {
  def withHostname(name: String): List[String] = {
    val localhost = InetAddress.getLocalHost.getHostName
    val dots = localhost.split("\\.").toList

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
    names.foldLeft(defaults) {
      case (c, cname) =>
        val config = ConfigFactory.parseResources(cname)
        config.withFallback(c)
    }
  }
}


object AkaneConfig {
  lazy val default = Configuration.makeConfigFor("akane")
}

class JumanConfig(val executable: String, val encoding: String, val params: List[String])
object JumanConfig {
  val jumanEx = "akane.juman.executable"
  val jumanArgs = "akane.juman.args"
  val jumanEncoding = "akane.juman.encoding"

  def apply(config: Config = ConfigFactory.empty()) = {
    import scala.collection.JavaConversions._
    val merged = config.withFallback(AkaneConfig.default)
    val exec = merged.getString(jumanEx)
    val enc = merged.getString(jumanEncoding)
    val args = merged.getStringList(jumanArgs).toList
    new JumanConfig(exec, enc, args)
  }
}

class KnpConfig(val juman: JumanConfig, val executable: String, val params: List[String])
object KnpConfig {
  val knpEx = "akane.knp.executable"
  val knpArgs = "akane.knp.args"

  def apply(config: Config = ConfigFactory.empty()) = {
    import scala.collection.JavaConversions._
    val juman = JumanConfig(config)
    val merged = config.withFallback(AkaneConfig.default)
    val knp = merged.getString(knpEx)
    val args = merged.getStringList(knpArgs).toList
    new KnpConfig(juman, knp, args)
  }
}