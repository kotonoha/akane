package ws.kotonoha.akane.checkers

import ws.kotonoha.akane.analyzers.juman.JumanStylePos
import ws.kotonoha.akane.analyzers.knp.{LexemeApi, PosAccess}
import ws.kotonoha.akane.parser.JumanPosSet

import scala.language.implicitConversions

trait PosCheck extends Serializable {
  def check(lex: PosAccess): Boolean
  def check(pos: JumanStylePos): Boolean

  def unapply(lex: LexemeApi): Option[LexemeApi] = {
    if (check(lex)) {
      Some(lex)
    } else None
  }
}

class JumanPosCheckers(posInfo: JumanPosSet) {
  def pos(name: String): PosCheck = {
    val id = posInfo.pos.find(_.name == name).map(_.num)
      .getOrElse(throw new Exception(s"pos name $name is invalid"))
    new OnlyPosChecker(id, name)
  }

  def posSubPos(name: String, subname: String): PosCheck = {
    val id = posInfo.pos.find(_.name == name).map(_.num)
      .getOrElse(throw new Exception(s"pos name $name is invalid"))

    val subid = posInfo.pos(id).subtypes.find(_.name == subname).map(_.num)
      .getOrElse(throw new Exception(s"subpos name $subname is invalid"))

    new PosSubposChecker(name, subname, id, subid)
  }

  object implicits {
    implicit def string2PosId(name: String): PosCheck = pos(name)
  }
}

object JumanPosCheckers {
  def default = new JumanPosCheckers(JumanPosSet.default)
}

final class OnlyPosChecker(id: Int, name: String) extends PosCheck {
  def check(lexeme: PosAccess): Boolean = {
    check(lexeme.pos)
  }

  override def check(pos: JumanStylePos): Boolean = pos.pos == id

  override def toString = s"$name:$id"
}

final class PosSubposChecker(name: String, subname: String, id: Int, subid: Int) extends PosCheck {
  override def check(lex: PosAccess): Boolean = {
    check(lex.pos)
  }

  override def check(pos: JumanStylePos): Boolean = {
    pos.pos == id && pos.subpos == subid
  }

  override def toString = s"$name:$id $subname:$subid"
}
