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

import java.io._

import com.typesafe.config.{Config, ConfigFactory}
import com.typesafe.scalalogging.StrictLogging
import ws.kotonoha.akane.analyzers.knp.raw.{KnpNode, OldAngUglyKnpTable}
import ws.kotonoha.akane.config.KnpConfig
import ws.kotonoha.akane.helpers.lisp.{KList, LispParser}
import ws.kotonoha.akane.parser.KnpTabFormatParser
import ws.kotonoha.akane.pipe.{AbstractRetryExecutor, Analyzer, Pipe}

import scala.util.parsing.input.CharSequenceReader

/**
  * @author eiennohito
  * @since 2013-09-03
  */
trait KnpProcessContainer extends Closeable {
  def output: OutputStream
  def input: InputStream
}

class SingleProcessKnpContainer(process: Process) extends KnpProcessContainer {
  def output = process.getOutputStream

  def input = process.getInputStream

  def close() {
    process.destroy()
  }
}

class PipedProcessKnpContaner(juman: Process, knp: Process, pipe: Pipe)
    extends KnpProcessContainer {
  def input = knp.getInputStream

  def output = juman.getOutputStream

  def close() {
    knp.destroy()
    juman.destroy()
    pipe.close()
  }
}

trait KnpResultParser {
  type Result
  def parse(reader: BufferedReader): Result
}

class KnpResultParserImpl extends KnpTabFormatParser with KnpResultParser {
  override type Result = Option[OldAngUglyKnpTable]
  override def parse(lines: TraversableOnce[CharSequence]) = super.parse(lines)
}

class KnpPipeAnalyzer[RParser <: KnpResultParser](
    cont: KnpProcessContainer,
    enc: String,
    parser: RParser)
    extends Analyzer[RParser#Result]
    with StrictLogging {

  def close() {
    cont.close()
  }

  def analyze(in: String): RParser#Result = {
    val writer = new OutputStreamWriter(cont.output, enc)
    val reader = new InputStreamReader(cont.input, enc)

    writer.write(in)
    writer.write("\n")
    writer.flush()

    val rd = new BufferedReader(reader)
    parser.parse(rd)
  }
}

class SexpKnpResultParser extends KnpResultParser with StrictLogging {
  override type Result = Option[KnpNode]

  val parser = LispParser.list

  override def parse(rd: BufferedReader) = {
    val stringBuilder = new StringBuilder
    var continue = true
    do {
      val line = rd.readLine()
      if (line == "EOS") {
        continue = false
      }
      stringBuilder.append(line).append("\n")
    } while (continue)

    val parseInput = new CharSequenceReader(stringBuilder)
    val lisp = parser(parseInput) match {
      case LispParser.Success(res, _) => Some(res.asInstanceOf[KList])
      case x                          => logger.warn("can't parse knp output " + x); None
    }
    lisp.flatMap(KnpSexpParser.parseTree)
  }
}

class KnpTreePipeParser private (factory: () => KnpPipeAnalyzer[SexpKnpResultParser])
    extends AbstractRetryExecutor[Option[KnpNode]](factory)
object KnpTreePipeParser {
  def apply(config: Config = ConfigFactory.empty()) = {
    val knpConfig = KnpConfig.apply(config)
    val factory = new KnpProcessFactory(knpConfig, KnpOutputType.sexp)
    new KnpTreePipeParser(() =>
      new KnpPipeAnalyzer(factory.launch(), knpConfig.juman.encoding, new SexpKnpResultParser))
  }
}

class KnpTabPipeParser private (factory: () => KnpPipeAnalyzer[KnpResultParserImpl])
    extends AbstractRetryExecutor[Option[OldAngUglyKnpTable]](factory)
object KnpTabPipeParser {
  def apply(config: Config = ConfigFactory.empty()) = {
    val knpConfig = KnpConfig.apply(config)
    val factory = new KnpProcessFactory(knpConfig, KnpOutputType.tab)
    new KnpTabPipeParser(() =>
      new KnpPipeAnalyzer(factory.launch(), knpConfig.juman.encoding, new KnpResultParserImpl))
  }
}
