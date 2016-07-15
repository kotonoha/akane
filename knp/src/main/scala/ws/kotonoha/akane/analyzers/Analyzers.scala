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

package ws.kotonoha.akane.analyzers

import java.io.{Closeable, InputStream, OutputStream}

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.io.IOUtils

import scala.annotation.tailrec
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

/**
 * @author eiennohito
 * @since 2015/09/10
 */
trait SyncAnalyzer[I, O] {
  def analyzeSync(input: I): Try[O]
}

trait AsyncAnalyzer[I, O] {
  def analyze(input: I)(implicit ec: ExecutionContext): Future[O]
}

trait SyncAsyncAnalyzer[I, O] extends SyncAnalyzer[I, O] with AsyncAnalyzer[I, O]

trait ToStream[T] {
  def writeTo(s: OutputStream, obj: T): Unit
}

trait FromStream[T] {
  /**
   * Should not wrap IOExceptions into try
   * @param s
   * @return
   */
  def readFrom(s: InputStream): Try[T]
}


trait SyncToAsyncAnalyzer[I, O] extends AsyncAnalyzer[I, O] { self: SyncAnalyzer[I, O] =>
  override def analyze(input: I)(implicit executionContext: ExecutionContext) = Future.fromTry(analyzeSync(input))
}

class AnalyzerException(reason: String) extends RuntimeException(reason)
