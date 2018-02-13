package ws.kotonoha.akane.config

import java.util.concurrent.TimeUnit

import com.typesafe.config.{Config, ConfigException}

import scala.concurrent.duration.FiniteDuration
import scala.languageFeature.implicitConversions

/**
  * @author eiennohito
  * @since 2016/05/04
  */
object ScalaConfig {
  implicit class RichConfig(val conf: Config) extends AnyVal {
    private def opt[T](f: => T) =
      try {
        Some(f)
      } catch {
        case e: ConfigException.Missing => None
      }

    private def optElse[@specialized T](f: => T, x: T) =
      try {
        f
      } catch {
        case e: ConfigException.Missing => x
      }

    @inline
    private[this] def optElse[T](name: String, extract: Config => T, other: => T): T = {
      if (conf.hasPath(name)) {
        extract(conf)
      } else other
    }

    @inline
    private[this] def optName[T](name: String, f: Config => T): Option[T] = {
      if (conf.hasPath(name)) {
        Some(f(conf))
      } else None
    }

    def optDouble(name: String): Option[Double] = opt(conf.getDouble(name))
    def optStr(name: String): Option[String] = opt(conf.getString(name))
    def doubleOr(name: String, default: Double): Double = optElse(conf.getDouble(name), default)
    def intOr(name: String, default: Int): Int = optElse(conf.getInt(name), default)
    def strOr(name: String, default: String): String = optElse(name, _.getString(name), default)
    def optBool(name: String): Option[Boolean] = {
      optName(name, _.getBoolean(name))
    }

    def optInt(name: String): Option[Int] = optName(name, _.getInt(name))
    def finiteDuration(name: String): FiniteDuration = {
      val millis = conf.getDuration(name, TimeUnit.MILLISECONDS)
      FiniteDuration(millis, TimeUnit.MILLISECONDS)
    }

    def optDuration(name: String): Option[FiniteDuration] =
      optName(name, cfg => {
        finiteDuration(name)
      })
  }
}

trait ScalaConfig {
  import ScalaConfig.RichConfig
  import scala.language.implicitConversions

  implicit def config2RichConfig(config: Config): RichConfig = new RichConfig(config)
}
