/*
 * Copyright 2012-2016 eiennohito (Tolmachev Arseny)
 *
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

package ws.kotonoha.akane.resources

import java.io.{
  BufferedReader,
  Closeable,
  IOException,
  InputStream,
  InputStreamReader,
  OutputStream
}
import java.nio.charset.Charset
import java.nio.file._
import java.nio.file.attribute.{BasicFileAttributes, FileTime}
import java.util.Collections
import java.util.function.BiPredicate

import ws.kotonoha.akane.io.{CloseableIterator, FrameAdapter, StreamClosableIterator}

import scala.util.Try

/**
  * @author eiennohito
  * @since 2015/09/24
  */
object FSPaths {
  import scala.collection.JavaConverters._

  def calculateRecursiveSize(path: Path): Long = {
    var len = 0L
    val str = Files.walk(path)

    for (str <- Files.walk(path).res) {
      val iter = str.iterator()
      while (iter.hasNext) {
        val p = iter.next()
        if (Files.isRegularFile(p)) {
          len += Files.size(p)
        }
      }
    }

    len
  }

  def recursiveWalk(root: Path, nameGlob: String): CloseableIterator[Path] = {
    new CloseableIterator[Path] {
      private val matcher = root.getFileSystem.getPathMatcher("glob:" + nameGlob)
      private val stream = Files.walk(root, FileVisitOption.FOLLOW_LINKS)
      private val iter = stream.iterator().asScala.filter { p =>
        Files.isRegularFile(p) && {
          val fname = p.getFileName
          matcher.matches(fname)
        }
      }
      override def hasNext = iter.hasNext
      override def next() = iter.next()
      override def close() = stream.close()
    }
  }

  def recursiveDelete(path: Path): Path = {
    Files.walkFileTree(
      path,
      new SimpleFileVisitor[Path] {
        override def visitFile(file: Path, attrs: BasicFileAttributes) = {
          Files.delete(file)
          FileVisitResult.CONTINUE
        }

        override def postVisitDirectory(dir: Path, exc: IOException) = {
          exc match {
            case null =>
              Files.delete(dir)
              FileVisitResult.CONTINUE
            case _ => throw exc
          }
        }
      }
    )
  }

  def children(p: Path, opts: FileVisitOption*): CloseableIterator[Path] = {
    val stream = Files.walk(p, 1, opts: _*)
    new StreamClosableIterator[Path](stream)
  }

  def deleteChildren(path: Path): Unit = {
    val stream = Files.newDirectoryStream(path)

    for (s <- stream.res) {
      val i = s.iterator()
      while (i.hasNext) {
        val p = i.next()
        if (Files.isDirectory(p)) {
          recursiveDelete(p)
        } else Files.delete(p)
      }
    }
  }

  def ensureDir(p: Path): Path = {
    if (Files.notExists(p)) {
      Files.createDirectories(p)
    } else {
      deleteChildren(p)
    }
    p
  }

  def ensureParent(p: Path): Path = {
    p.parent.foreach(x => if (x.notExists) x.mkdirs())
    p
  }

  def extension(path: Path): String = {
    val name = path.getFileName.toString
    val pos = name.lastIndexOf('.')
    if (pos == -1) ""
    else {
      name.substring(pos + 1)
    }
  }

  def writeLines(p: Path, lines: TraversableOnce[String], enc: Charset): Path = {
    try {
      val ret = Files.write(p, new java.lang.Iterable[String] {
        override def iterator() = lines.toIterator.asJava
      }, enc)
      ret
    } finally {
      lines match {
        case l: Closeable => l.close()
        case _            =>
      }
    }
  }

  def writeStrings(
      p: Path,
      strings: TraversableOnce[String],
      separator: String,
      enc: Charset): Path = {
    try {
      val ret = Files.write(
        p,
        new java.lang.Iterable[String] {
          val iter: Iterator[String] = strings.toIterator
          override def iterator = new java.util.Iterator[String] {
            private var haveSep = false
            override def hasNext = iter.hasNext || haveSep
            override def next() = {
              if (haveSep) {
                haveSep = false
                separator
              } else {
                haveSep = iter.hasNext
                iter.next()
              }
            }
          }
        },
        enc
      )
      ret
    } finally {
      strings match {
        case l: Closeable => l.close()
        case _            => //nop
      }
    }
  }

  def find(path: Path, depth: Int = Int.MaxValue)(
      fn: (Path, BasicFileAttributes) => Boolean): AutoClosableWrapper[CloseableIterator[Path]] = {
    new CloseableIterator[Path] {
      private val stream = Files.find(path, depth, new BiPredicate[Path, BasicFileAttributes] {
        override def test(t: Path, u: BasicFileAttributes) = fn(t, u)
      })
      private val iter = stream.iterator()
      override def next() = iter.next()
      override def hasNext = iter.hasNext
      override def close() = stream.close()
    }.res
  }

  implicit class RichString(val s: String) extends AnyVal {
    def p: Path = Paths.get(s)
  }

  implicit class AutoClosableOps[T <: AutoCloseable](val obj: T) extends AnyVal {
    def res: AutoClosableWrapper[T] = new AutoClosableWrapper[T](obj)
  }

  //need to convert inlines to macros for even better inlining
  //no need to hurry though
  class AutoClosableWrapper[T <: AutoCloseable](val obj: T) extends AnyVal {
    @inline
    def foreach(f: T => Unit): Unit = {
      try {
        f(obj)
      } finally {
        obj.close()
      }
    }

    //@inline
    def map[R](f: T => R): EmptyWrapper[R] = {
      try {
        new EmptyWrapper[R](f(obj))
      } finally {
        obj.close()
      }
    }

    def either[R](fn: T => R): Either[Exception, R] =
      try {
        Right(fn(obj))
      } catch {
        case e: Exception => Left(e)
      }

    def exec[R](fn: T => R): Try[R] = {
      try {
        scala.util.Success(fn(obj))
      } catch {
        case e: Exception => scala.util.Failure(e)
      }
    }
  }

  class EmptyWrapper[T](val obj: T) extends AnyVal {
    @inline
    def map[R](f: T => R): EmptyWrapper[R] = new EmptyWrapper[R](f(obj))

    @inline
    def foreach(f: T => Unit): Unit = f(obj)
  }

  implicit class RichPath(val p: Path) extends AnyVal {
    def modTime: FileTime = Files.getLastModifiedTime(p)
    def fileSize: Long = Files.size(p)
    def exists: Boolean = Files.exists(p)
    def notExists: Boolean = Files.notExists(p)
    def delete(): Unit = Files.delete(p)
    def parent: Option[Path] = Option(p.getParent)
    def name: String = p.getFileName.toString
    def extension: String = FSPaths.extension(p)
    def deleteIfExists(): Boolean = Files.deleteIfExists(p)
    def copyTo(p2: Path): Path = Files.copy(p, p2)
    def moveTo(p2: Path): Path = Files.move(p, p2)
    def lines(enc: Charset = utf8): CloseableIterator[String] = wrapStream(Files.lines(p, enc))
    def ensureDirectory(): Path = FSPaths.ensureDir(p)
    def ensureParent(): Path = FSPaths.ensureParent(p)
    def outputStream(openOption: OpenOption*): AutoClosableWrapper[OutputStream] =
      Files.newOutputStream(p, openOption: _*).res
    def walk(globPattern: String): CloseableIterator[Path] = FSPaths.recursiveWalk(p, globPattern)
    def children(opts: FileVisitOption*): CloseableIterator[Path] = FSPaths.children(p, opts: _*)

    def mkdirs(): Path = Files.createDirectories(p)

    def inputStream: AutoClosableWrapper[InputStream] = Files.newInputStream(p).res
    def framedBy(sep: Array[Byte]): FrameAdapter = FrameAdapter.apply(p.inputStream.obj, sep)
    def bufferedReader(cs: Charset = utf8): AutoClosableWrapper[BufferedReader] = {
      val stream = Files.newInputStream(p)
      val rdr = new InputStreamReader(stream, cs)
      val brdr = new BufferedReader(rdr)
      brdr.res
    }

    def write(s: CharSequence, enc: Charset = utf8): Path = {
      Files.write(p, Collections.singleton(s), enc)
    }

    def writeLines(lines: TraversableOnce[String], enc: Charset = utf8): Path =
      FSPaths.writeLines(p, lines, enc)
    def writeStrings(
        strings: TraversableOnce[String],
        separator: String,
        enc: Charset = utf8): Path = FSPaths.writeStrings(p, strings, separator, enc)

    def /(s: String): Path = p.resolve(s)
    def /(px: Path): Path = p.resolve(px)
  }

  implicit class RichFileTime(val t: FileTime) extends AnyVal {
    def <(o: FileTime): Boolean = t.compareTo(o) < 0
    def >(o: FileTime): Boolean = t.compareTo(o) > 0
  }

  val utf8: Charset = Charset.forName("utf-8")

  private def wrapStream[T](str: java.util.stream.Stream[T]): CloseableIterator[T] =
    new StreamClosableIterator(str)
}
