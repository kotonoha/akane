package ws.kotonoha.akane.blobdb.util

import java.nio.ByteBuffer

import ws.kotonoha.akane.blobdb.api.ResultCreator

import scala.util.control.NonFatal

/**
  * @author eiennohito
  * @since 2016/07/19
  */
class StringResultCreator extends ResultCreator[String] {
  def result(buf: ByteBuffer): Option[String] = try {
    if (buf.hasArray) {
      Some(new String(buf.array(), buf.arrayOffset() + buf.position(), buf.remaining(), "utf-8"))
    } else ???
  } catch {
    case NonFatal(_) => None
  }
}
