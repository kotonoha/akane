package ws.kotonoha

import org.scalatest._
import matchers.ShouldMatchers
import java.io.{OutputStream, InputStream}
import sys.process._

/**User: Dijkstra Date: 16.08.12 Time: 11:00 */
class SocketlessIO {
  val JUMAN_COMMAND = """"C:\\Program Files\\juman\\juman.exe -S""""
  var in : InputStream = null
  var out : OutputStream = null
  val io = new ProcessIO(
    stdout => out = stdout,
    stdin => in = stdin,
    _ => {}
  )
  val process = JUMAN_COMMAND.run(io)
  wait(1000)
  out.write("あんた馬鹿！".getBytes)
  print("Send output")
  var input = in.read()
  while (input != -1) {
    print(input.toChar)
    input = in.read()
  }
  process.destroy()
}

class SocketlessJumanImplTest extends FlatSpec with ShouldMatchers {
  "Socketless io" should "interact" in {
     val a = new SocketlessIO()
  }

}
