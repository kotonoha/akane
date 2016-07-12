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
