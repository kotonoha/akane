package ws.kotonoha.akane.blobdb

import java.lang.{Long => JLong}
import java.nio.ByteBuffer
import java.nio.file.{Files, Path}
import java.util.concurrent.atomic.AtomicInteger

import akka.actor.ActorSystem
import org.mapdb.BTreeKeySerializer
import org.scalatest.{BeforeAndAfterAll, FreeSpec, Matchers}
import ws.kotonoha.akane.blobdb.api.{BlobDbConfig, IdOps}
import ws.kotonoha.akane.blobdb.impl.BlDbImpl
import ws.kotonoha.akane.blobdb.util.IdentityBlobTransformer

import scala.concurrent.Await
import scala.util.Random

object LongIdOps extends IdOps[JLong] {
  private val atomicInt = new AtomicInteger()
  override def generate(prefix: Int) = JLong.valueOf(prefix.toLong << 32 | atomicInt.getAndIncrement())
  override def checkPrefix(o: JLong, prefix: Int) = (o.longValue() >>> 32) == prefix
  override def comparator = serializer.getComparator
  override def serializer = BTreeKeySerializer.ZERO_OR_POSITIVE_LONG
}


/**
 * @author eiennohito
 * @since 2014-10-15
 */
class CompressedBlobDbImplTest extends FreeSpec with Matchers with BeforeAndAfterAll {

  import scala.concurrent.duration._


  val asys = ActorSystem("test")

  override protected def afterAll() = {
    super.afterAll()
    asys.terminate()
  }

  def makeDb(path: Path) = {
    val cfg = BlobDbConfig(
      root = path,
      forCommit = asys,
      cacheEc = asys.dispatcher,
      shardMaxSize = 2 * 1024 * 1024
    )

    val impl = new BlDbImpl[JLong](cfg, LongIdOps, new IdentityBlobTransformer)
    impl
  }

  def prepareDb(input: TraversableOnce[Array[Byte]]) = {

    val directory = Files.createTempDirectory("compblobtest")
    val impl = makeDb(directory)

    val creator = impl.writer()
    val importer = creator.defaultImporter()
    val refs = input.flatMap { arr =>
      val buf = ByteBuffer.wrap(arr)
      importer.pushData(buf)
    }.toList

    Await.result(importer.commit(refs), 1.minute)

    impl.close()

    (directory, refs.map(_.id))
  }

  import ws.kotonoha.akane.resources.FSPaths._

  "TreeDatabase" - {
    "lz4" - {
      "can handle simple things" in {
        val input = List("test1", "test2", "test3")
        val data = input.map(_.getBytes("utf-8"))
        val (path, oids) = prepareDb(data)
        for (db <- makeDb(path).res) {
          val output = oids.flatMap(db.get)

          input should equal (output)
        }
      }

      "works with mmap window lesser than buffer" in {
        val rng = new Random(0)
        val bytes = (1 to 5) map {_ =>
          val bytes = new Array[Byte](30000)
          rng.nextBytes(bytes)
          bytes.map(x => (0x30 + (x % 10)).toByte)
        }

        val input = bytes.map(x => new String(x)).toList
        val (path, oids) = prepareDb(bytes)
        for (db <- makeDb(path).res) {
          val output = oids.flatMap(db.get)

          input should equal (output)
        }
      }
    }
  }
}
