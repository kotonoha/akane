package ws.kotonoha.akane.blobdb.impl

import java.nio.ByteBuffer
import java.util.function.Supplier

import net.jpountz.lz4.{LZ4BlockOutputStream2, LZ4Factory}
import ws.kotonoha.akane.blobdb.impl.bgz.BlockGunzipper

/**
 * @author eiennohito
 * @since 2014-10-24
 */
trait BlockReader {
  /**
   * Read compressed block from bytebuffer, starting from position
   * @param buffer in the LE byte order
   * @param position position to read from
   * @return decompressed data with compressed length
   */
  def readBlock(buffer: ByteBuffer, position: Int): DecompressedBuffer
}

class ZlibBlockReader extends BlockReader {

  import ws.kotonoha.akane.blobdb.impl.bgz.BlockCompressedStreamConstants._

  //Inflater is stateful, so it cannot be shared between threads
  val unzipper = ThreadLocal.withInitial(new Supplier[BlockGunzipper] {
    override def get() = new BlockGunzipper
  })

  def readBlock(buffer: ByteBuffer, position: Int) = {
    val compressedBuf = new Array[Byte](MAX_COMPRESSED_BLOCK_SIZE)
    val uncomprBuf = new Array[Byte](DEFAULT_UNCOMPRESSED_BLOCK_SIZE)
    val comLen = (buffer.getInt(position + BLOCK_LENGTH_OFFSET) & 0xffff) + 1
    val buf = buffer.duplicate()
    buf.position(position)
    buf.get(compressedBuf, 0, BLOCK_HEADER_LENGTH + comLen)

    val len = unzipper.get().unzipBlock(uncomprBuf, compressedBuf, comLen)
    val arr = java.util.Arrays.copyOfRange(uncomprBuf, 0, len)
    DecompressedBuffer(comLen, arr)
  }
}

class LZ4BlockReader extends BlockReader {

  val decompressor = LZ4Factory.fastestInstance().fastDecompressor()

  override def readBlock(buffer: ByteBuffer, position: Int) = {
    val compressedDataLength = buffer.getInt(position + LZ4BlockOutputStream2.MAGIC_LENGTH + 1)
    val compressedBufferLength = compressedDataLength + LZ4BlockOutputStream2.HEADER_LENGTH
    val deLen = buffer.getInt(position + LZ4BlockOutputStream2.MAGIC_LENGTH + 5)
    val method = buffer.get(position + LZ4BlockOutputStream2.MAGIC_LENGTH) & 0xF0
    val outBuf = new Array[Byte](deLen)
    val buf = buffer.duplicate()
    buf.position(position + LZ4BlockOutputStream2.HEADER_LENGTH)
    if (method == LZ4BlockOutputStream2.COMPRESSION_METHOD_RAW) {
      buf.get(outBuf)
      DecompressedBuffer(compressedBufferLength, outBuf)
    } else {
      buf.limit(buf.position() + compressedDataLength)
      val outwrapped = ByteBuffer.wrap(outBuf)
      decompressor.decompress(buf, outwrapped)
      DecompressedBuffer(compressedBufferLength, outBuf)
    }
  }
}
