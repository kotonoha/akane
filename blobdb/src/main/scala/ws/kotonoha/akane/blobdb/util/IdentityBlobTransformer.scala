package ws.kotonoha.akane.blobdb.util

import java.io.OutputStream
import java.nio.ByteBuffer

import ws.kotonoha.akane.blobdb.api.{BlobTransformer, IdRef}

/**
  * @author eiennohito
  * @since 2016/07/19
  */
class IdentityBlobTransformer[K <: AnyRef] extends BlobTransformer[K] {
  override def transformWrite(ref: IdRef[K], data: ByteBuffer, output: OutputStream) = {
    val len = data.remaining()
    if (data.hasArray) {
      output.write(data.array(), data.arrayOffset() + data.position(), len)
    } else {
      val buf = new Array[Byte](4096)
      var rem = len
      while (rem > 0) {
        data.get(buf)
        val rd = len - data.remaining()
        output.write(buf, 0, rd)
        rem -= rd
      }
    }
    len
  }
}
