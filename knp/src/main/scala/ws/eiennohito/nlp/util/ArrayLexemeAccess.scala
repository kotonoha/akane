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
