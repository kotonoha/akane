/*
 * Copyright 2012 eiennohito
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

package ws.kotonoha.akane.conjuation

/**
 * @author eiennohito
 * @since 13.11.12
 */

case class ConjObject(items: List[String]) {
  def without(s: String) = {
    val a = items match {
      case x :: xs if x.endsWith(s) => x.substring(0, x.length - s.length) :: xs
      case _ => Nil
    }
    new ConjObject(a)
  }

  def render: Option[String] = {
    items match {
      case Nil => None
      case _ => Some(items.reverse.mkString)
    }
  }

  def last(cnt: Int) = items.headOption flatMap(i => {
    val len = i.length
    if (len < cnt) None
    else {
      Some(i.substring(len - cnt))
    }
  })

  def add(s: String) = {
    items match {
      case Nil => this
      case x => new ConjObject(s :: x)
    }
  }
}

object ConjObject {
  val Empty = new ConjObject(Nil)
}

trait Renderable {
  protected def obj: ConjObject

  def render = obj.render
}

case class TaForm(obj: ConjObject) extends Renderable


case class TeForm(obj: ConjObject) extends Renderable {
  def iru = new IchidanVerb(obj.add("いる"))
}

case class TaStem(obj: ConjObject, patalize: Boolean = false) extends Renderable {
  def ta: TaForm = patalize match {
    case true => new TaForm(obj.add("だ"))
    case false => new TaForm(obj.add("た"))
  }
  def te: TeForm = patalize match {
    case true => new TeForm(obj.add("で"))
    case false => new TeForm(obj.add("て"))
  }
}

case class NaiStem(obj: ConjObject) extends Renderable {

}

trait Verb extends Renderable {
  def taStem: TaStem

  //shortcuts
  def past = taStem ta
  def teForm = taStem te
}

object Verb {
  def ichidan(s: String) = new IchidanVerb(new ConjObject(s :: Nil))
}

class IchidanVerb(protected val obj: ConjObject) extends Verb {
  def stem = obj.without("る")
  def taStem = TaStem(stem)
  def naiStem = NaiStem(stem)
}

class GodanVerb(protected val obj: ConjObject) extends Verb {
  def taStem = {
    obj.last(1) match {
      case Some(c @ ("う" | "る" | "つ")) => new TaStem(obj.without(c).add("っ"))
      case Some(c @ ("ぶ" | "む" | "ぬ")) => new TaStem(obj.without(c).add("ん"), true)
      case Some(c @ "く") => new TaStem(obj.without(c).add("い"))
      case Some(c @ "ぐ") => new TaStem(obj.without(c).add("い"), true)
      case Some(c @ "す") => new TaStem(obj.without(c).add("し"))
      case _ => new TaStem(ConjObject.Empty)
    }
  }
}
