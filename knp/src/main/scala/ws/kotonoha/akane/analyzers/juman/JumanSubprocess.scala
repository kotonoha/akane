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

package ws.kotonoha.akane.analyzers.juman

import java.io._
import java.nio.charset.Charset
import java.util

import com.typesafe.scalalogging.StrictLogging
import org.apache.commons.lang3.StringUtils
import ws.kotonoha.akane.analyzers._
import ws.kotonoha.akane.utils.ParseUtil

import scala.collection.mutable
import scala.collection.mutable.ListBuffer
import scala.util.Success


trait JumanAnalyzer extends SyncAnalyzer[String, JumanSequence]
trait AsyncJumanAnalyzer extends AsyncAnalyzer[String, JumanSequence]

/**
 * Actually this is a blocking analyzer using juman in a subprocess mode
 *
 * @author eiennohito
 * @since 2015/09/10
 */
object JumanSubprocess extends StrictLogging {

  def process(config: JumanConfig): Process = {
    val lst = new util.ArrayList[String]()
    logger.debug(s"trying to start subprocess juman as: ${config.executable}")
    lst.add(config.executable)
    config.params.foreach(lst.add)
    val procBldr = new ProcessBuilder(lst)
    val proc = procBldr.start()
    logger.debug(s"process.start() succeeded, alive=${proc.isAlive}")
    proc
  }

  def create(config: JumanConfig): JumanAnalyzer with SubprocessControls = {
    new Impl(config)
  }

  def reader(cfg: JumanConfig): FromStream[JumanSequence] = JumanText.reader(cfg.encoding)

  def writer(cfg: JumanConfig): ToStream[String] = new ToStream[String] {
    val eol = 0x0A //End of line
    val cs = Charset.forName(cfg.encoding)

    override def writeTo(s: OutputStream, obj: String) = {
      val res = cs.encode(obj)
      s.write(res.array(), res.arrayOffset() + res.position(), res.limit())
      s.write(eol)
    }
  }

  private final class Impl(cfg: JumanConfig) extends
    SpawnedProcessAnalyzer[String, JumanSequence](process(cfg))(writer(cfg), reader(cfg))
    with JumanAnalyzer
}

object JumanText {

  def reader(enc: String) = new FromStream[JumanSequence] {
    private val cs = Charset.forName(enc)

    override def readFrom(s: InputStream) = try {
      val lexemes = new mutable.ArrayBuffer[JumanLexeme]
      val reader = new BufferedReader(new InputStreamReader(s, cs))
      var ok = true
      while (ok) {
        val line = reader.readLine()
        if (line == null || line == "EOS") {
          ok = false
        } else {
          if (line.startsWith("@")) {
            val item = lexemes(lexemes.length - 1)
            lexemes(lexemes.length - 1) = item.copy(variants = item.variants :+ JumanText.parseLine(line, 1, line.length))
          } else lexemes += JumanText.parseLine(line, 0, line.length)
        }
      }
      Success(JumanSequence(lexemes.result()))
    } catch {
      case e: IOException => throw e
      case e: Exception => scala.util.Failure(e)
    }
  }

  def parseOptions(input: CharSequence, start: Int, end: Int): Seq[JumanOption] = {
    val sub = input.subSequence(start, end)

    var stchr = StringUtils.indexOf(sub, '"', 0)
    if (stchr == -1) return Nil
    stchr += 1
    val theend = StringUtils.indexOf(sub, '"', stchr)
    if (theend == -1) return Nil

    parseOptionsInner(sub, stchr, theend)
  }

  def parseOptionsInner(sub: CharSequence, start: Int, theend: Int): Seq[JumanOption] = {
    var stchr = start
    val bldr = new ListBuffer[JumanOption]
    var stend = StringUtils.indexOf(sub, ' ', stchr)
    if (stend == -1) {
      stend = theend
    }

    while (stchr < theend) {
      val semi = StringUtils.indexOf(sub, ':', stchr)
      if (semi == -1 || semi >= stend) {
        bldr += JumanOption(key = sub.subSequence(stchr, stend).toString)
      } else {
        bldr += JumanOption(
          key = sub.subSequence(stchr, semi).toString,
          value = Some(sub.subSequence(semi + 1, stend).toString)
        )
      }

      val temp = stend + 1
      stend = StringUtils.indexOf(sub, ' ', temp)
      stchr = temp
      if (stend == -1) {
        stend = theend
      }
    }

    bldr.result()
  }

  def parseLine(seq: CharSequence, start: Int, end: Int): JumanLexeme = {
    val end0 = start
    val end1 = StringUtils.indexOf(seq, ' ', end0 + 1)
    val end2 = StringUtils.indexOf(seq, ' ', end1 + 1)
    val end3 = StringUtils.indexOf(seq, ' ', end2 + 1)
    val end4 = StringUtils.indexOf(seq, ' ', end3 + 1)
    val end5 = StringUtils.indexOf(seq, ' ', end4 + 1)
    val end6 = StringUtils.indexOf(seq, ' ', end5 + 1)
    val end7 = StringUtils.indexOf(seq, ' ', end6 + 1)
    val end8 = StringUtils.indexOf(seq, ' ', end7 + 1)
    val end9 = StringUtils.indexOf(seq, ' ', end8 + 1)
    val end10 = StringUtils.indexOf(seq, ' ', end9 + 1)
    var end11 = StringUtils.indexOf(seq, ' ', end10 + 1)

    if (end11 == -1 || end11 > end) {
      end11 = end
    }

    JumanLexeme(
      surface = seq.subSequence(end0, end1).toString,
      reading = seq.subSequence(end1 + 1, end2).toString,
      baseform = seq.subSequence(end2 + 1, end3).toString,
      posInfo = JumanPos(
        ParseUtil.parseInt(seq, end4 + 1, end5),
        ParseUtil.parseInt(seq, end6 + 1, end7),
        ParseUtil.parseInt(seq, end8 + 1, end9),
        ParseUtil.parseInt(seq, end10 + 1, end11)
      ),
      if (end11 == end) {
        Nil
      } else {
        parseOptions(seq, end11 + 1, end)
      }
    )
  }
}
