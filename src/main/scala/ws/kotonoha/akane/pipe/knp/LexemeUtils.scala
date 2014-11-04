package ws.kotonoha.akane.pipe.knp

/**
 * @author eiennohito
 * @since 2014-10-31
 */
object LexemeUtils {
  private val lexemeInit = "KnpLexeme".hashCode

  def hashKnpLexeme(lexeme: KnpLexeme) = {
    lexeme.dicForm.##
  }
}
