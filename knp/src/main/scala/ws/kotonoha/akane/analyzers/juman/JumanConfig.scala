package ws.kotonoha.akane.analyzers.juman

import com.typesafe.config.{Config, ConfigFactory}
import ws.kotonoha.akane.config.AkaneConfig

/**
  * @author eiennohito
  * @since 2016/07/12
  */
class JumanConfig(val executable: String, val encoding: String, val params: Seq[String])

object JumanConfig {

  import scala.collection.JavaConverters._

  val jumanEx = "akane.juman.executable"
  val jumanArgs = "akane.juman.args"
  val jumanEncoding = "akane.juman.encoding"

  def apply(config: Config = ConfigFactory.empty()) = {
    val merged = config.withFallback(AkaneConfig.default)
    val exec = merged.getString(jumanEx)
    val enc = merged.getString(jumanEncoding)
    val args = merged.getStringList(jumanArgs).asScala
    new JumanConfig(exec, enc, args)
  }
}
