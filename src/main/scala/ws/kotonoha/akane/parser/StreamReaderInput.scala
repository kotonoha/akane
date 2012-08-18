package ws.kotonoha.akane.parser

import java.io.InputStreamReader

/**
 * @author eiennohito
 * @since 17.08.12
 */

class StreamReaderInput(in: InputStreamReader) extends AozoraInput {
  private var nv = in.read()

  def peek = nv

  //doesn't go forward
  def next = {
    val n = nv
    nv = in.read()
    n
  }
}
