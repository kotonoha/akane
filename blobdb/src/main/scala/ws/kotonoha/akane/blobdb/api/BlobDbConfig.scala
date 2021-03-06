package ws.kotonoha.akane.blobdb.api

import java.io.OutputStream
import java.nio.file.{Files, Path, StandardOpenOption}

import akka.actor.ActorRefFactory
import net.jpountz.lz4.{LZ4BlockOutputStream2, LZ4Factory}
import net.jpountz.xxhash.XXHashFactory
import ws.kotonoha.akane.blobdb.impl.{
  BlockReader,
  LZ4BlockReader,
  TreeDatabaseException,
  ZlibBlockReader
}
import ws.kotonoha.akane.resources.FSPaths

import scala.concurrent.ExecutionContextExecutor

/**
  * @author eiennohito
  * @since 2016/07/19
  */
case class BlobDbConfig(
    root: Path,
    forCommit: ActorRefFactory,
    cacheEc: ExecutionContextExecutor,
    name: String = "blob",
    idPrefix: Int = 0,
    shardMaxSize: Int = 500 * 1024 * 1024,
    decompressedCache: Long = 50 * 1024 * 1024,
    codec: BlobDbCodec = BlobDbCodec.lz4
) {

  def guessFileForNum(num: Int): Path = {
    val pattern = f"$num%08d.$name."

    val stream = FSPaths.find(root, 1) { (p, a) =>
      val fname = p.getFileName.toString
      a.isRegularFile && fname.startsWith(pattern)
    }

    val res = for (s <- stream) yield {
      val candidates = s.toList
      if (candidates.size == 1) {
        candidates.head
      } else {
        throw new TreeDatabaseException(
          s"When searching for database file with pattern $pattern, found ${candidates.mkString("[", ",", "]")}")
      }
    }
    res.obj
  }

  def pathFile(num: Int) = {
    val fname = f"$num%08d.$name.${codec.extension}"
    root.resolve(fname)
  }
}

trait BlockWriter {
  def stream: OutputStream
  def position: Long
  def blockAddress: Long
  def flush(): Unit
}

trait BlobDbCodec {
  def extension: String
  def reader(): BlockReader
  def writer(file: Path): BlockWriter
}

object BlobDbCodec {

  object lz4 extends BlobDbCodec {
    override def extension = "lz4"
    override def reader() = new LZ4BlockReader
    override def writer(file: Path) = new BlockWriter {
      override val stream: LZ4BlockOutputStream2 = {
        val raw = Files.newOutputStream(file, StandardOpenOption.CREATE)
        val wrapped = new LZ4BlockOutputStream2(
          raw,
          1024 * 64, //64 block
          LZ4Factory.fastestInstance().highCompressor(17),
          XXHashFactory.fastestInstance().newStreamingHash32(0).asChecksum(),
          true
        )
        wrapped
      }
      override def position = stream.getFilePointer
      override def blockAddress = stream.getBlockAddress
      override def flush(): Unit = stream.flush()
    }
  }

  object gzip extends BlobDbCodec {
    override def extension = "gz"
    override def reader() = new ZlibBlockReader
    override def writer(file: Path) = ???
  }

  def guess(ext: String, cfg: BlobDbConfig): BlobDbCodec = {
    val codec = cfg.codec
    if (ext == codec.extension) {
      codec
    } else {
      ext match {
        case "lz4" => lz4
        case "gz"  => gzip
        case _     => throw new Exception(s"unknown extension for blobdb: $ext")
      }
    }
  }

}
