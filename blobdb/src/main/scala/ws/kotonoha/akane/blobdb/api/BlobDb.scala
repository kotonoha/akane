package ws.kotonoha.akane.blobdb.api

import java.io.OutputStream
import java.nio.ByteBuffer
import java.util.Comparator

import org.mapdb.BTreeKeySerializer
import ws.kotonoha.akane.blobdb.impl.BlobIndexEntry

import scala.concurrent.Future

/**
  * @author eiennohito
  * @since 2016/07/19
  */
trait BlobDb[Key <: AnyRef] {
  def ops: IdOps[Key]

  def infoFor(id: Key): Option[BlobIndexEntry]
  def valueGetter[Val](rc: ResultCreator[Val]): ItemSearch[Key, Val]
  def get(id: Key): Option[String]
  def idCount: Long
  def idIter(): Iterator[Key]
  def writer(): BlobDbWriter[Key]
}

trait ResultCreator[T] {
  def result(buf: ByteBuffer): Option[T]
}

trait IdOps[K <: AnyRef] {
  def generate(prefix: Int): K
  def checkPrefix(o: K, prefix: Int): Boolean
  def comparator: Comparator[K]
  def serializer: BTreeKeySerializer[K]
}

trait ItemSearch[Key <: AnyRef, +Value] { self =>
  def get(id: Key): Option[Value]

  def map[Y](f: (Value, Key) => Y): ItemSearch[Key, Y] = new ItemSearch[Key, Y] {
    override def get(id: Key) = self.get(id).map(f(_, id))
  }
}

trait BlobDbWriter[Key <: AnyRef] {
  def makeImporter(tf: BlobTransformer[Key]): BlobImporter[Key]
  def defaultImporter(): BlobImporter[Key]
  def delete(ids: Seq[Key]): Future[TrOk]
}

trait BlobImporter[Key <: AnyRef] {
  def pushData(data: ByteBuffer): Option[DataRef[Key]]
  def commit(refs: Seq[DataRef[Key]]): Future[TrOk]
  def rollback(): Future[TrOk]
}

trait BlobTransformer[Key <: AnyRef] {
  def transformWrite(ref: IdRef[Key], input: ByteBuffer, output: OutputStream): Int
}
