package ws.kotonoha.akane.pipe.knp

import ws.kotonoha.akane.analyzers.knp.LexemeApi

import scala.util.hashing.MurmurHash3

/**
 * @author eiennohito
 * @since 2014-10-31
 */
object LexemeUtils {
  private val lexemeInit = "KnpLexeme".hashCode

  def hashKnpLexeme(lexeme: LexemeApi) = {
    MurmurHash3.stringHash(lexeme.dicForm, lexemeInit)
  }
}
