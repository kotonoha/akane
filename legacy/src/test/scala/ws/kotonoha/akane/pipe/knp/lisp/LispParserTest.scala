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

package ws.kotonoha.akane.pipe.knp.lisp

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers

import scala.util.parsing.input.{CharSequenceReader, Reader, StreamReader}
import java.io.InputStreamReader

import ws.kotonoha.akane.helpers.lisp.{KAtom, KItems, LispParser}

/**
 * @author eiennohito
 * @since 2013-09-04
 */
class LispParserTest extends FreeSpec with ShouldMatchers {

  implicit def input(s: String): Reader[Char] = {
    new CharSequenceReader(s, 0)
  }

  "lisp parser" - {
    "parses small list" in {
      val res = LispParser.list("(the info)")
      res match {
        case LispParser.Success(KItems(the, info), _) =>
          the should equal (KAtom("the"))
          info should equal (KAtom("info"))
      }
    }

    "parses a knp output" in {
      val resource = getClass.getClassLoader.getResourceAsStream("knp.answer.txt")
      val reader = new InputStreamReader(resource, "utf-8")
      val input = StreamReader.apply(reader)
      val result = LispParser.parser(input)
      result.successful should be (true)
      resource.close()
    }
  }

}
