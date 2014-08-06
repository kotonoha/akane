package ws.kotonoha.akane.pipe

import java.io.Closeable
import com.typesafe.scalalogging.StrictLogging

import scala.util.control.NonFatal

/**
 * @author eiennohito
 * @since 2013-09-03
 */

trait Analyzer[RType] extends Closeable {
  def analyze(input: String): RType
}

class AbstractRetryExecutor[RType] (factory: () => Analyzer[RType]) extends StrictLogging with Closeable {
  val maxRetries = 3

  var analyzer: Analyzer[RType] = _

  def parse(input: String): RType = {
    if (analyzer == null)
      analyzer = factory()
    parseInner(input, 0)
  }

  private def parseInner(input: String, trial: Int): RType = {
    try {
      analyzer.analyze(input)
    } catch {
      case NonFatal(e) =>
        if (trial < maxRetries) {
          logger.warn(s"Error when analyzing, retrying $trial out of $maxRetries", e)
          try { analyzer.close() } catch {
            case NonFatal(t) => logger.error("Exception when closing inner analyzer", t)
          }
          analyzer = factory() //create a new analyzer
          parseInner(input, trial + 1)
        } else throw e
    }
  }

  def close() {
    analyzer.close()
  }
}
