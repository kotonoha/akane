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

package ws.kotonoha.akane.analyzers.knp

/**
  * @author eiennohito
  * @since 2015/09/18
  */
trait FeatureAccess {
  def valueOfFeature(name: String): Option[String]
  def featureExists(name: String): Boolean
  def featureKeys: Traversable[String]
}

trait LexemeAccess {
  def lexeme(idx: Int): LexemeApi
  def lexemeStart: Int
  def lexemeEnd: Int
  def lexemeCnt: Int

  def lexemeIter: Iterator[LexemeApi] = new Iterator[LexemeApi] {
    private var pos = lexemeStart
    override def hasNext = pos < lexemeEnd
    override def next() = {
      val l = lexeme(pos)
      pos += 1
      l
    }
  }

  def charLength = {
    var i = lexemeStart
    val e = lexemeEnd
    var len = 0
    while (i < e) {
      val l = lexeme(i)
      len += l.surface.length
      i += 1
    }
    len
  }
}

trait KihonkuAccess {
  def kihonku(idx: Int): KihonkuApi
  def kihonkuStart: Int
  def kihonkuCnt: Int
  def kihonkuEnd: Int

  def kihonkuIter: Iterator[KihonkuApi] = new Iterator[KihonkuApi] {
    private var pos = kihonkuStart
    override def hasNext = pos < kihonkuEnd
    override def next() = {
      val k = kihonku(pos)
      pos += 1
      k
    }
  }
}

trait BunsetsuAccess {
  def bunsetsu(idx: Int): BunsetsuApi
  def bunsetsuStart: Int
  def bunsetsuCnt: Int
  def bunsetsuEnd: Int

  def bunsetsuIter: Iterator[BunsetsuApi] = new Iterator[BunsetsuApi] {
    private var pos = bunsetsuStart
    override def hasNext = pos < bunsetsuEnd
    override def next() = {
      val b = bunsetsu(pos)
      pos += 1
      b
    }
  }
}
