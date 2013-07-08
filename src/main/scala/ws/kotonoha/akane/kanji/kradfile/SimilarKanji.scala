/*
 * Copyright 2012-2013 eiennohito
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

package ws.kotonoha.akane.kanji.kradfile

/**
 * @author eiennohito
 * @since 08.07.13 
 */

case class SimilarKanji(text: String, rads: Seq[String], common: Seq[String], diff: Seq[String], score: Int)

object SimilarKanji {

  def findSimilar(rads: Seq[String]) = {
    val cands = RadicalDb.reverse(rads.head) //get all kanji that have first radical
    cands.map { k =>
      val krads = RadicalDb.table(k)
      val common = rads.intersect(krads)
      val other = krads.diff(common)
      val score = 10 * common.length - 3 * other.length
      SimilarKanji(k, krads, common, other, score)
    } filter(_.score > 0) sortBy(-_.score)
  }

  def find(k: String) = {
    //for first we find all radicals of our kanji
    val rads = RadicalDb.table.get(k)
    rads match {
      case Some(x) => findSimilar(x) filterNot(_.text == k)
      case None => Seq.empty
    }
  }
}
