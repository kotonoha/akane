package ws.kotonoha.akane.checkers

import ws.kotonoha.akane.analyzers.knp.LexemeApi

import scala.language.implicitConversions
import scala.languageFeature.implicitConversions

/**
  * @author eiennohito
  * @since 2016/01/15
  */

trait LexemeChecker extends Serializable {
  def check(lex: LexemeApi): Boolean
}

final class DicFormLexemeChecker(form: String) extends LexemeChecker {
  override def check(lex: LexemeApi) = lex.dicForm.equals(form)
}

final class DicFormExceptChecker(form: String) extends LexemeChecker {
  override def check(lex: LexemeApi) = !lex.dicForm.equals(form)
}

final class PosLexemeChecker(posCheck: PosCheck) extends LexemeChecker {
  override def check(lex: LexemeApi) = posCheck.check(lex.pos)
}

final class PosAndLexemeChecker(pc: PosCheck, other: LexemeChecker) extends LexemeChecker {
  override def check(lex: LexemeApi) = {
    pc.check(lex.pos) && other.check(lex)
  }
}

final class CanonicalFormChecker(form: String) extends LexemeChecker {
  override def check(lex: LexemeApi): Boolean = lex.canonicForm().equals(form)
}

object LexemeCheckers {
  implicit def posChecker2LexemeChecher(pc: PosCheck): LexemeChecker = new PosLexemeChecker(pc)

  implicit class PosCheckerApi (val pc: PosCheck) extends AnyVal {
    def withDicForm(form: String): LexemeChecker = new PosAndLexemeChecker(pc, new DicFormLexemeChecker(form))
    def exceptDicForm(form: String): LexemeChecker = new PosAndLexemeChecker(pc, new DicFormExceptChecker(form))
  }
}
