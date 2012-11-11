/*
 * Copyright 2012 eiennohito
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

package ws.kotonoha.akane.tools

import scalax.file.Path
import org.apache.commons.lang.StringUtils

/**
 * @author eiennohito
 * @since 10.11.12 
 */

object Filter {
  def main(args: Array[String]) = {
    val filter = Path.fromString(args(1))
    val flt = filter.lines() map (_.split("\t")) flatMap {
      case Array(s1, _) => {
        Some(StringUtils.strip(s1, "\""))
      }
      case _ => None
    } toSet

    val items = Path.fromString(args(0))
    val present = items.lines().map(_.trim).filter{x => flt.contains(x)}
    val out = Path.fromString(args(0) + ".out")
    out.writeStrings(present, "\n")
  }
}
