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

package ws.kotonoha.akane.pipe.knp

import java.util

import ws.kotonoha.akane.config.KnpConfig

import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext

/**
  * @author eiennohito
  * @since 2013-09-03
  */
object KnpOutputType extends Enumeration {
  val tab = Value("tab")
  val sexp = Value("sexp")

  type KnpOutputType = KnpOutputType.Value
}

class KnpProcessFactory(config: KnpConfig, kind: KnpOutputType.Value)(
    implicit val ec: ExecutionContext = ExecutionContext.global) {

  private def winLaunch() = {
    val args = new util.ArrayList[String]()
    args.add("cmd")
    args.add("/c")
    args.add(config.juman.executable.replaceAll("/", "\\\\"))
    config.juman.params.foreach(x => args.add(x.replaceAll("/", "\\\\")))
    args.add("|")
    args.add(config.executable.replaceAll("/", "\\\\"))
    config.params.foreach(x => args.add(x.replaceAll("/", "\\\\")))
    args.add(normKind)
    val pb = new ProcessBuilder(args)
    new SingleProcessKnpContainer(pb.start())
  }

  def launch(): KnpProcessContainer = {
    System.getProperty("os.name") match {
      case x if x.contains("Windows") => winLaunch()
      case _                          => unixLaunch()
    }
  }

  lazy val normKind = kind match {
    case KnpOutputType.tab  => "-tab"
    case KnpOutputType.sexp => "-sexp"
    case _                  => throw new KnpInitException(s"invalid knp output type: $kind")
  }

  private def unixLaunch() = {

    val args = new util.ArrayList[String]()
    args.add("/bin/sh")
    args.add("-c")

    val quoted = new ListBuffer[String]

    val juman = config.juman
    quoted += juman.executable
    quoted ++= juman.params
    quoted += "|"
    quoted += config.executable
    quoted ++= config.params
    quoted += normKind

    args.add(quoted.mkString(" "))

    val pb = new ProcessBuilder(args)

    new SingleProcessKnpContainer(pb.start())
  }
}

class KnpInitException(msg: String) extends RuntimeException(msg)
