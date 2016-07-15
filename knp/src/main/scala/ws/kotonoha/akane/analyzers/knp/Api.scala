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

import scala.collection.mutable

/**
 * @author eiennohito
 * @since 2015/09/18
 */
trait TableApi extends LexemeAccess with BunsetsuAccess with KihonkuAccess {
  def kihonkuIdxForSurface(pos: Int): Int = {
    var i = 0
    var cnt = 0
    val blen = kihonkuCnt
    while (i < blen) {
      val b = kihonku(i)
      var j = b.lexemeStart
      val jend = b.lexemeEnd

      while (j < jend) {
        val lex = lexeme(j)
        cnt += lex.surface.length
        if (cnt > pos)
          return i
        j += 1
      }

      i += 1
    }
    return -1
  }

  /**
   * Transforms kihonku scope to bunsetsu scope
   * @param kihonkuScope sorted array of kihonku indexes
   * @return array of bunsetsu indices
   */
  def bunsetsuScope(kihonkuScope: Array[Int]): Array[Int] = {
    val indices = new mutable.BitSet()

    var curKih = 0

    var i = 0
    var cnt = 0
    val blen = bunsetsuCnt

    while (i < blen) {
      val bnst = bunsetsu(i)

      cnt += bnst.kihonkuCnt

      while (curKih < kihonkuScope.length &&
        kihonkuScope(curKih) < cnt) {
        indices += i
        curKih += 1
      }

      i += 1
    }

    indices.toArray
  }

  def bunsetsuIdxForKihonku(kih: Int): Int = {

    var i = 0
    var cnt = 0
    val blen = bunsetsuCnt

    while (i < blen) {
      val bnst = bunsetsu(i)

      cnt += bnst.kihonkuCnt

      if (cnt > kih) return i

      i += 1
    }

    -1
  }

}

trait DependencyApi {
  def number: Int
  def depNumber: Int
  def depType: String
}

trait BunsetsuApi extends LexemeAccess with KihonkuAccess with FeatureAccess with DependencyApi {

}

trait KihonkuApi extends LexemeAccess with FeatureAccess with DependencyApi {

}

trait LexemeApi extends JapaneseLexeme
