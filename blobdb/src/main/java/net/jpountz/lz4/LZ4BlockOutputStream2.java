package net.jpountz.lz4;

/*
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

import net.jpountz.xxhash.StreamingXXHash32;
import net.jpountz.xxhash.XXHashFactory;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.Checksum;

/**
 * Streaming LZ4.
 * <p>
 * This class compresses data into fixed-size blocks of compressed data.
 * @see LZ4BlockInputStream
 */
public final class LZ4BlockOutputStream2 extends FilterOutputStream {

  static final byte[] MAGIC = new byte[] { 'L', 'Z', '4', 'B', 'l', 'o', 'c', 'k' };
  public static final int MAGIC_LENGTH = MAGIC.length;

  public static final int HEADER_LENGTH =
      MAGIC_LENGTH // magic bytes
          + 1          // token
          + 4          // compressed length
          + 4          // decompressed length
          + 4;         // checksum

  static final int COMPRESSION_LEVEL_BASE = 10;
  static final int MIN_BLOCK_SIZE = 64;
  static final int MAX_BLOCK_SIZE = 1 << (COMPRESSION_LEVEL_BASE + 0x0F);

  public static final int COMPRESSION_METHOD_RAW = 0x10;
  public static final int COMPRESSION_METHOD_LZ4 = 0x20;

  static final int DEFAULT_SEED = 0x9747b28c;

  private static int compressionLevel(int blockSize) {
    if (blockSize < MIN_BLOCK_SIZE) {
      throw new IllegalArgumentException("blockSize must be >= " + MIN_BLOCK_SIZE + ", got " + blockSize);
    } else if (blockSize > MAX_BLOCK_SIZE) {
      throw new IllegalArgumentException("blockSize must be <= " + MAX_BLOCK_SIZE + ", got " + blockSize);
    }
    int compressionLevel = 32 - Integer.numberOfLeadingZeros(blockSize - 1); // ceil of log2
    assert (1 << compressionLevel) >= blockSize;
    assert blockSize * 2 > (1 << compressionLevel);
    compressionLevel = Math.max(0, compressionLevel - COMPRESSION_LEVEL_BASE);
    assert compressionLevel >= 0 && compressionLevel <= 0x0F;
    return compressionLevel;
  }

  private final int blockSize;
  private final int compressionLevel;
  private final LZ4Compressor compressor;
  private final Checksum checksum;
  private final byte[] buffer;
  private final byte[] compressedBuffer;
  private final boolean syncFlush;
  private boolean finished;
  private int uncompressedOffset;
  private long compPosition = 0L;

  /**
   * Create a new {@link OutputStream} with configurable block size. Large
   * blocks require more memory at compression and decompression time but
   * should improve the compression ratio.
   *
   * @param out         the {@link OutputStream} to feed
   * @param blockSize   the maximum number of bytes to try to compress at once,
   *                    must be >= 64 and <= 32 M
   * @param compressor  the {@link LZ4Compressor} instance to use to compress
   *                    data
   * @param checksum    the {@link Checksum} instance to use to check data for
   *                    integrity.
   * @param syncFlush   true if pending data should also be flushed on {@link #flush()}
   */
  public LZ4BlockOutputStream2(OutputStream out, int blockSize, LZ4Compressor compressor, Checksum checksum, boolean syncFlush) {
    super(out);
    this.blockSize = blockSize;
    this.compressor = compressor;
    this.checksum = checksum;
    this.compressionLevel = compressionLevel(blockSize);
    this.buffer = new byte[blockSize];
    final int compressedBlockSize = HEADER_LENGTH + compressor.maxCompressedLength(blockSize);
    this.compressedBuffer = new byte[compressedBlockSize];
    this.syncFlush = syncFlush;
    uncompressedOffset = 0;
    finished = false;
    System.arraycopy(MAGIC, 0, compressedBuffer, 0, MAGIC_LENGTH);
  }

  /**
   * Create a new instance which checks stream integrity using
   * {@link StreamingXXHash32} and doesn't sync flush.
   * @see #LZ4BlockOutputStream2(OutputStream, int, LZ4Compressor, Checksum, boolean)
   * @see StreamingXXHash32#asChecksum()
   */
  public LZ4BlockOutputStream2(OutputStream out, int blockSize, LZ4Compressor compressor) {
    this(out, blockSize, compressor, XXHashFactory.fastestInstance().newStreamingHash32(DEFAULT_SEED).asChecksum(), false);
  }

  /**
   * Create a new instance which compresses with the standard LZ4 compression
   * algorithm.
   * @see #LZ4BlockOutputStream2(OutputStream, int, LZ4Compressor)
   * @see LZ4Factory#fastCompressor()
   */
  public LZ4BlockOutputStream2(OutputStream out, int blockSize) {
    this(out, blockSize, LZ4Factory.fastestInstance().fastCompressor());
  }

  /**
   * Create a new instance which compresses into blocks of 64 KB.
   * @see #LZ4BlockOutputStream2(OutputStream, int)
   */
  public LZ4BlockOutputStream2(OutputStream out) {
    this(out, 1 << 16);
  }

  private void ensureNotFinished() {
    if (finished) {
      throw new IllegalStateException("This stream is already closed");
    }
  }

  @Override
  public void write(int b) throws IOException {
    ensureNotFinished();
    if (uncompressedOffset == blockSize) {
      flushBufferedData();
    }
    buffer[uncompressedOffset++] = (byte) b;
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    assert off >= 0;
    assert len >= 0;
    assert b.length <= (off + len);
    ensureNotFinished();

    while (uncompressedOffset + len >= blockSize) {
      final int toCopy = blockSize - uncompressedOffset;
      System.arraycopy(b, off, buffer, uncompressedOffset, toCopy);
      uncompressedOffset = blockSize;
      flushBufferedData();
      off += toCopy;
      len -= toCopy;
    }
    System.arraycopy(b, off, buffer, uncompressedOffset, len);
    uncompressedOffset += len;
  }

  @Override
  public void write(byte[] b) throws IOException {
    ensureNotFinished();
    write(b, 0, b.length);
  }

  @Override
  public void close() throws IOException {
    if (!finished) {
      finish();
    }
    if (out != null) {
      out.close();
      out = null;
    }
  }

  public long getFilePointer() {
    assert uncompressedOffset <= 0xffff;
    return (uncompressedOffset & 0xffff) | (compPosition << 16);
  }

  public long getBlockAddress() {
    return compPosition;
  }

  private void flushBufferedData() throws IOException {
    if (uncompressedOffset == 0) {
      return;
    }
    checksum.reset();
    checksum.update(buffer, 0, uncompressedOffset);
    final int check = (int) checksum.getValue();
    int compressedLength = compressor.compress(buffer, 0, uncompressedOffset, compressedBuffer, HEADER_LENGTH);
    final int compressMethod;
    if (compressedLength >= uncompressedOffset) {
      compressMethod = COMPRESSION_METHOD_RAW;
      compressedLength = uncompressedOffset;
      System.arraycopy(buffer, 0, compressedBuffer, HEADER_LENGTH, uncompressedOffset);
    } else {
      compressMethod = COMPRESSION_METHOD_LZ4;
    }

    compressedBuffer[MAGIC_LENGTH] = (byte) (compressMethod | compressionLevel);
    writeIntLE(compressedLength, compressedBuffer, MAGIC_LENGTH + 1);
    writeIntLE(uncompressedOffset, compressedBuffer, MAGIC_LENGTH + 5);
    writeIntLE(check, compressedBuffer, MAGIC_LENGTH + 9);
    assert MAGIC_LENGTH + 13 == HEADER_LENGTH;
    int writeLen = HEADER_LENGTH + compressedLength;
    out.write(compressedBuffer, 0, writeLen);
    compPosition += writeLen;
    uncompressedOffset = 0;
  }

  /**
   * Flush this compressed {@link OutputStream}.
   *
   * If the stream has been created with <code>syncFlush=true</code>, pending
   * data will be compressed and appended to the underlying {@link OutputStream}
   * before calling {@link OutputStream#flush()} on the underlying stream.
   * Otherwise, this method just flushes the underlying stream, so pending
   * data might not be available for reading until {@link #finish()} or
   * {@link #close()} is called.
   */
  @Override
  public void flush() throws IOException {
    if (syncFlush) {
      flushBufferedData();
    }
    out.flush();
  }

  /**
   * Same as {@link #close()} except that it doesn't close the underlying stream.
   * This can be useful if you want to keep on using the underlying stream.
   */
  public void finish() throws IOException {
    ensureNotFinished();
    flushBufferedData();
    compressedBuffer[MAGIC_LENGTH] = (byte) (COMPRESSION_METHOD_RAW | compressionLevel);
    writeIntLE(0, compressedBuffer, MAGIC_LENGTH + 1);
    writeIntLE(0, compressedBuffer, MAGIC_LENGTH + 5);
    writeIntLE(0, compressedBuffer, MAGIC_LENGTH + 9);
    assert MAGIC_LENGTH + 13 == HEADER_LENGTH;
    out.write(compressedBuffer, 0, HEADER_LENGTH);
    finished = true;
    out.flush();
  }

  private static void writeIntLE(int i, byte[] buf, int off) {
    buf[off++] = (byte) i;
    buf[off++] = (byte) (i >>> 8);
    buf[off++] = (byte) (i >>> 16);
    buf[off++] = (byte) (i >>> 24);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + "(out=" + out + ", blockSize=" + blockSize
        + ", compressor=" + compressor + ", checksum=" + checksum + ")";
  }

}
