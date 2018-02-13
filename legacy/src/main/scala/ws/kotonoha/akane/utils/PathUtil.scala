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

package ws.kotonoha.akane.utils

import scalax.file.Path
import scalax.file.PathMatcher.{Exists, GlobNameMatcher}
import collection.mutable
import scalax.io.Codec

/**
  * @author eiennohito
  * @since 30.10.12
  */
object PathUtil {
  def enumeratePaths(in: TraversableOnce[Path]): Iterator[Path] = {
    in.map { p =>
        (p.parent, p.name)
      }
      .map {
        case (Some(dir), nm) => dir ** GlobNameMatcher(nm)
        case (None, nm)      => Path.fromString(nm)
      }
      .reduce(_ +++ _)
      .iterator
      .filter(Exists)
  }

  def enumerateStrings(in: TraversableOnce[String]): Iterator[Path] = {
    enumeratePaths(in.map(Path.fromString(_)))
  }

  def stoplist(in: Iterator[Path]): Set[String] = {
    in.foldLeft(new mutable.HashSet[String]()) {
        case (hs, p) => {
          p.lines()(Codec.UTF8)
            .filter(!_.startsWith("#"))
            .map(w => w.split("\\|").map(_.trim).filter(_.length > 0))
            .foreach { hs ++= _ }
          hs
        }
      }
      .toSet
  }
}
