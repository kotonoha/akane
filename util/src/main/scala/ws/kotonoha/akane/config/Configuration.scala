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

package ws.kotonoha.akane.config

import java.net.InetAddress

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging

/**
  * @author eiennohito
  * @since 2013-09-02
  */
object Configuration extends StrictLogging {

  private val theOverrides = ConfigFactory.defaultOverrides()

  def withHostname(name: String): List[String] = {
    val localhost = InetAddress.getLocalHost.getHostName
    val dots: List[String] = localhost.split("\\.").toList

    (dots.scanLeft(List(name)) {
      case (l, part) => part :: l
    } ++ dots.scanRight(List[String](name)) {
      case (l, part) => l :: part
    }).map(_.mkString(".")).filter(_.length > 2).distinct
  }

  def withUsername(
      name: String,
      config: Config = ConfigFactory.defaultOverrides()): List[String] = {
    val cfg = config.withFallback(theOverrides)
    val uname = cfg.getString("user.name")
    s"$uname.$name" :: Nil
  }

  def possibleNamesFor(
      name: String,
      defaults: Config = ConfigFactory.defaultOverrides()): List[String] = {
    val c1 = name :: withHostname(name) ::: withUsername(name, defaults).flatMap(withHostname)
    c1.map(_ + ".conf").distinct
  }

  def rawConfigFor(name: String, defaults: Config = ConfigFactory.defaultOverrides()): Config = {
    val names = possibleNamesFor(name, defaults)
    logger.debug(s"For config {$name} trying [${names.mkString(", ")}]")
    val cfg = names.foldLeft(defaults) {
      case (c, cname) =>
        val config = ConfigFactory.parseResources(getClass.getClassLoader, cname)
        if (!config.isEmpty) {
          logger.debug(s"Loaded config from file $cname")
          config.withFallback(c)
        } else c
    }
    cfg
  }

  def makeConfigFor(name: String, defaults: Config = ConfigFactory.defaultOverrides()) = {
    rawConfigFor(name, defaults).resolve()
  }
}
