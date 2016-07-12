package ws.kotonoha.akane.config

import com.typesafe.config.{Config, ConfigFactory}
import ws.kotonoha.akane.analyzers.juman.JumanConfig


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
