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

package ws.kotonoha.akane.io

import java.io.Closeable

import scala.collection.GenTraversableOnce

/**
  * @author eiennohito
  * @since 2015/11/25
  */
trait CloseableIterator[+T] extends Iterator[T] with Closeable with AutoCloseable { self =>
  override def map[B](f: (T) => B): CloseableIterator[B] = new WrappedClosableIterator[B](self) {
    override def next() = f(self.next())
    override def hasNext = self.hasNext
  }

  override def flatMap[B](f: (T) => GenTraversableOnce[B]): CloseableIterator[B] = new WrappedClosableIterator[B](self) {
    var cached: Iterator[B] = null

    override def next(): B = {
      if (cached != null && cached.hasNext) {
        cached.next()
      } else {
        updateCached()
        this.next()
      }
    }

    private def updateCached(): Unit = {
      cached match {
        case x: CloseableIterator[_] => x.close()
        case _ =>
      }
      if (self.hasNext) {
        cached = f(self.next()).toIterator
      } else {
        cached = null
      }
    }

    override def hasNext = {
      if (cached == null) {
        if (self.hasNext) {
          updateCached()
          this.hasNext
        } else false
      } else {
        cached.hasNext || self.hasNext
      }
    }
  }

  override def foreach[U](f: (T) => U) = {
    try {
      super.foreach(f)
    } finally {
      close()
    }
  }

  override def filter(p: (T) => Boolean): CloseableIterator[T] = new WrappedClosableIterator[T](self) {
    var item: T = _
    var cached = false
    override def next() = {
      cached = false
      item
    }
    override def hasNext: Boolean = {
      if (cached) {
        true
      } else {
        cached = true
        while (self.hasNext) {
          item = self.next()
          if (p(item)) {
            return true
          }
        }
        false
      }
    }
  }

  override def zip[B](that: Iterator[B]): CloseableIterator[(T, B)] = {
    val wrapped = that match {
      case y: CloseableIterator[_] =>
        new Closeable {
          override def close() = {
            self.close()
            y.close()
          }
        }
      case _ => self
    }
    new WrappedClosableIterator[(T, B)](wrapped) {
      override def hasNext = that.hasNext && self.hasNext
      override def next() = (self.next(), that.next())
    }
  }

  override def toIterator = self
}

abstract class WrappedClosableIterator[+T](o: Closeable) extends CloseableIterator[T] {
  override def close() = o.close()
}

class StreamClosableIterator[+T](str: java.util.stream.Stream[T]) extends CloseableIterator[T] {
  import scala.collection.JavaConverters._

  private val iter = str.iterator().asScala

  override def hasNext = iter.hasNext
  override def next() = iter.next()
  override def close() = str.close()
}
