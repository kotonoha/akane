package ws.kotonoha.akane.utils

import java.io.BufferedReader

/**
  * @author eiennohito
  * @since 2015/12/09
  */
class DelimetedIterator(val reader: BufferedReader, stop: String) extends BufferedIterator[String] {

  var string: String = reader.readLine()

  override def head = string

  override def next() = {
    val data = string
    string = reader.readLine()
    data
  }

  override def hasNext = reader.ready() && string != stop
}
