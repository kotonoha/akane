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
