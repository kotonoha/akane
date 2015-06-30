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

package ws.kotonoha.akane.mecab

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers
import org.bridj.{BridJ, Pointer}
import java.nio.charset.Charset
import mecab.MecabLibrary
import java.lang.{Byte => JByte}

/**
 * @author eiennohito
 * @since 11.11.12 
 */

class OtherTest extends FreeSpec with ShouldMatchers {

  "asd" - {
    "dgs" in {
      //BridJ.register(classOf[mecab.Tagger])
      val byte = Pointer.allocateByte()
      byte.set(0.toByte)
      val t = MecabLibrary.mecab_new2(byte)
      val str = "私はバカです"
      val bytes = str.getBytes("UTF-8")
      //MecabLibrary.createTagger()
      val bts = Pointer.pointerToArray[JByte](bytes)
      val len: Long = bytes.length
      val data = MecabLibrary.mecab_sparse_tostr2(t, bts, len)


      //val data = t.get().parse(bts, len)
      val s = data.getStringAtOffset(0L, Pointer.StringType.C, Charset.forName("UTF-8"))
      //println(s)
      MecabLibrary.mecab_destroy(t)
    }
  }

}
