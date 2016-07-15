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

package ws.kotonoha.akane.juman

import org.scalatest.FreeSpec
import org.scalatest.matchers.ShouldMatchers

/**
 * @author eiennohito
 * @since 16.08.12
 */

class JumanPipeExecutorTest extends FreeSpec with ShouldMatchers{
 "juman" - {
   "runs and parses" in {
     val ex = JumanPipeExecutor() // should be in path
     val res = ex.parse("バカ猫が！")
     ex.close()
     res should have length (4)
     res(1).reading should equal ("ねこ")
   }

   "parses 2 times" in {
     val ex = JumanPipeExecutor()
     ex.parse("今")
     ex.parse("貰う")
     ex.close()
   }

   "test - okoru" in {
     val ex = JumanPipeExecutor()
     val lst = ex.parse("おこる")
     lst should have length(1)
     ex.close()
   }
 }
}
