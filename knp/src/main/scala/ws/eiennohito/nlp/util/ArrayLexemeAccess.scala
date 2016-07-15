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

package ws.eiennohito.nlp.util

import ws.kotonoha.akane.analyzers.knp.{LexemeAccess, LexemeApi}

import scala.language.implicitConversions

/**
 * @author eiennohito
 * @since 2015/09/18
 */

class ArrayLexemeAccess(lexs: IndexedSeq[LexemeApi]) extends LexemeAccess {
  override def lexeme(idx: Int) = lexs(idx)
  override def lexemeEnd = lexs.length
  override def lexemeCnt = lexs.length
  override def lexemeStart = 0
}

object ArrayLexemeAccess {
  implicit def wrapArray(lexs: Array[LexemeApi]): LexemeAccess = new ArrayLexemeAccess(lexs)
  implicit def wrapIndSeq(lexs: IndexedSeq[LexemeApi]): LexemeAccess = new ArrayLexemeAccess(lexs)
}
