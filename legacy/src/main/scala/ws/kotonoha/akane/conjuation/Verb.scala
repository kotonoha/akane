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
      case _                        => Nil
    }
    new ConjObject(a)
  }

  def without(len: Int) = {
    val a = items match {
      case x :: xs if (x.length >= len) => {
        if (x.length == len) xs
        else x.substring(0, x.length - len) :: xs
      }
      case _ => Nil
    }
    new ConjObject(a)
  }

  def render: Option[String] = {
    items match {
      case Nil => None
      case _   => Some(items.reverse.mkString)
    }
  }

  def last(cnt: Int) =
    items.headOption.flatMap(i => {
      val len = i.length
      if (len < cnt) None
      else {
        Some(i.substring(len - cnt))
      }
    })

  def add(s: String) = {
    items match {
      case Nil => this
      case x   => new ConjObject(s :: x)
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

case class ConjRule[T <: Renderable](tf: T => Renderable, chain: List[String]) {
  def chain[U <: Renderable](ctf: U => T, name: String): ConjRule[U] =
    ConjRule(tf.compose(ctf), name :: chain)
}

trait Chaining[T <: Chaining[T]] extends Renderable { self: T =>
  type Generator = Int => List[ConjRule[T]]
  protected def rules: List[Generator] = Nil

  protected def terminal = false

  lazy val selfrule: List[ConjRule[T]] = new ConjRule[T]((arg: T) => arg, Nil) :: Nil

  def generate(depth: Int): List[ConjRule[T]] = {
    assert(depth >= 0, "Depth should be non-negative")
    if (depth == 0 || rules.isEmpty) {
      return selfrule
    }
    val r = rules.flatMap(_(depth))
    if (terminal) selfrule ++ r else r
  }

  // hides T => U composition
  def make[U <: Chaining[U]](rule: T => U, name: String): Generator = {
    def generate(lvl: Int): List[ConjRule[T]] = {
      val u = rule(self)
      u.generate(lvl - 1).map(_.chain(rule, name))
    }
    generate
  }
}

case class TaForm(obj: ConjObject) extends Renderable with Chaining[TaForm]

case class TeForm(obj: ConjObject) extends Renderable with Chaining[TeForm] {
  def iru: Verb = new IchidanVerb(obj.add("いる"))

  override protected def terminal = true
  override protected def rules = make(_.iru, "iru") :: Nil
}

case class TaStem(obj: ConjObject, patalize: Boolean = false)
    extends Renderable
    with Chaining[TaStem] {
  def ta: TaForm = patalize match {
    case true  => new TaForm(obj.add("だ"))
    case false => new TaForm(obj.add("た"))
  }
  def te: TeForm = patalize match {
    case true  => new TeForm(obj.add("で"))
    case false => new TeForm(obj.add("て"))
  }

  override protected def rules = List(
    make(_.ta, "ta"),
    make(_.te, "te")
  )
}

class AdjStem(val obj: ConjObject) extends Chaining[AdjStem] {
  def sugiru: Verb = new IchidanVerb(obj.add("すぎる"))

  override protected def rules = List(
    make(_.sugiru, "sugiru")
  )
}

trait Adjective extends Renderable with Chaining[Adjective] {
  override protected def terminal = true
  def stem: AdjStem

  override protected def rules = List(
    make(_.stem, "adjStem")
  )
}

case class AdjI(obj: ConjObject) extends Adjective {
  def stem = new AdjStem(obj.without("い"))
}

class NaiAdjStem(obj: ConjObject) extends AdjStem(obj) {
  override def sugiru = new IchidanVerb(obj.add("さ").add("すぎる"))
}
case class Nai(obj: ConjObject) extends Adjective {
  def stem = new NaiAdjStem(obj.without("い"))
}

case class NaiStem(obj: ConjObject) extends Renderable with Chaining[NaiStem] {
  def nai: Adjective = Nai(obj.add("ない"))
  override protected def rules = make(_.nai, "nai") :: Nil
}

case class Masu(obj: ConjObject) extends Chaining[Masu] {
  def neg = Terminal(obj.without("す").add("せん"))
  def taStem = TaStem(obj.without("す").add("し"))

  override protected def terminal = true
  override protected def rules = List(make(_.neg, "neg"), make(_.taStem, "taStem"))
}

case class MasuStem(obj: ConjObject) extends Renderable with Chaining[MasuStem] {
  def masu = Masu(obj.add("ます"))
  def tai: Adjective = new AdjI(obj.add("たい"))
  def nasai = new Terminal(obj.add("なさい"))

  override protected def rules = List(
    make(_.masu, "masu"),
    make(_.tai, "tai"),
    make(_.nasai, "nasai")
  )
}

case class Terminal(obj: ConjObject) extends Chaining[Terminal] {
  override protected def terminal = true
}

case class MeireiForm(obj: ConjObject) extends Chaining[MeireiForm] {
  override protected def terminal = true
}

class NotImplementedException(msg: String) extends RuntimeException(msg)

trait Verb extends Renderable with Chaining[Verb] {
  def taStem: TaStem
  def naiStem: NaiStem
  def masuStem: MasuStem
  def kateiStem: KateiStem
  def possible: Verb
  def ukemi: Verb
  def sieki: Verb
  def siekiUkemi: Verb
  def you: Terminal
  def meirei: MeireiForm

  //shortcuts
  def past: TaForm = taStem.ta
  def teForm: TeForm = taStem.te

  def meireiNa = Terminal(obj.add("な"))

  override protected def terminal = true
  override protected def rules = List(
    make(_.naiStem, "naiStem"),
    make(_.masuStem, "masuStem"),
    make(_.taStem, "taStem"),
    make(_.kateiStem, "kateiStem"),
    make(_.possible, "possible"),
    make(_.ukemi, "ukemi"),
    make(_.you, "you"),
    make(_.meirei, "meirei"),
    make(_.meireiNa, "na")
  )
}

object AdjI {
  def apply(in: String) = new AdjI(ConjObject(in :: Nil))
}

object Verb {
  def dummy = new GodanVerb(ConjObject.Empty)

  val v5re = "v5.".r
  def fromJMDict(verb: String, tag: String): Verb = {
    tag match {
      case "v5k-s" => iku(verb)
      case "v1"    => ichidan(verb)
      case v5re()  => godan(verb)
      case _       => throw new NotImplementedException(tag)
    }
  }

  def godan(s: String) = {
    new GodanVerb(new ConjObject(s :: Nil))
  }

  def iku(s: String) = {
    new Iku(new ConjObject(s :: Nil))
  }

  def ichidan(s: String) = new IchidanVerb(new ConjObject(s :: Nil))
}

case class BaIf(obj: ConjObject) extends Renderable with Chaining[BaIf] {
  override protected def terminal = true
}

case class KateiStem(obj: ConjObject) extends Renderable with Chaining[KateiStem] {
  def ba = BaIf(obj.add("ば"))

  override protected def rules = List(make(_.ba, "ba"))
}

class IchidanVerb(protected val obj: ConjObject) extends Verb {
  def stem = obj.without("る")
  def taStem = TaStem(stem)
  def naiStem = NaiStem(stem)
  def masuStem = MasuStem(stem)
  def kateiStem = KateiStem(stem.add("れ"))
  def possible = new IchidanVerb(stem.add("られる"))
  def ukemi = new IchidanVerb(stem.add("られる"))
  def sieki = new IchidanVerb(stem.add("させる"))
  def siekiUkemi = new IchidanVerb(stem.add("される"))
  def you = Terminal(stem.add("よう"))
  def meirei = MeireiForm(stem.add("ろ"))
}

object GodanMappings {
  val ua = Map("う" -> "わ",
               "く" -> "か",
               "す" -> "さ",
               "つ" -> "た",
               "ぬ" -> "な",
               "ふ" -> "は",
               "む" -> "ま",
               "る" -> "ら",
               "ぐ" -> "が",
               "ず" -> "ざ",
               "づ" -> "だ",
               "ぶ" -> "ば",
               "ぷ" -> "ぱ")

  val ui = Map("う" -> "い",
               "く" -> "き",
               "す" -> "し",
               "つ" -> "ち",
               "ぬ" -> "に",
               "ふ" -> "ひ",
               "む" -> "み",
               "る" -> "り",
               "ぐ" -> "ぎ",
               "ず" -> "じ",
               "づ" -> "ぢ",
               "ぶ" -> "ぴ",
               "ぷ" -> "び")

  val ue = Map("う" -> "え",
               "く" -> "け",
               "す" -> "せ",
               "つ" -> "て",
               "ぬ" -> "ね",
               "ふ" -> "へ",
               "む" -> "め",
               "る" -> "れ",
               "ぐ" -> "げ",
               "ず" -> "ぜ",
               "づ" -> "で",
               "ぶ" -> "べ",
               "ぷ" -> "ぺ")

  val uo = Map("う" -> "お",
               "く" -> "こ",
               "す" -> "そ",
               "つ" -> "と",
               "ぬ" -> "の",
               "ふ" -> "ほ",
               "む" -> "も",
               "る" -> "ろ",
               "ぐ" -> "ご",
               "ず" -> "ぞ",
               "づ" -> "ど",
               "ぶ" -> "ぼ",
               "ぷ" -> "ぽ")
}

class GodanVerb(protected val obj: ConjObject) extends Verb {
  def taStem = {
    obj.last(1) match {
      case Some(c @ ("う" | "る" | "つ")) => new TaStem(obj.without(c).add("っ"))
      case Some(c @ ("ぶ" | "む" | "ぬ")) => new TaStem(obj.without(c).add("ん"), true)
      case Some(c @ "く")               => new TaStem(obj.without(c).add("い"))
      case Some(c @ "ぐ")               => new TaStem(obj.without(c).add("い"), true)
      case Some(c @ "す")               => new TaStem(obj.without(c).add("し"))
      case _                           => new TaStem(ConjObject.Empty)
    }
  }

  def stemWithMap(in: Map[String, String]) = {
    obj.last(1).flatMap(in.get(_)) match {
      case Some(c) => obj.without(1).add(c)
      case _       => ConjObject.Empty
    }
  }

  def naiStem = {
    NaiStem(stemWithMap(GodanMappings.ua))
  }

  def masuStem = {
    MasuStem(stemWithMap(GodanMappings.ui))
  }

  def kateiStem = {
    KateiStem(stemWithMap(GodanMappings.ue))
  }

  def possible = new IchidanVerb(kateiStem.obj.add("る"))

  def ukemi = new IchidanVerb(naiStem.obj.add("れる"))

  def sieki = new IchidanVerb(naiStem.obj.add("せる"))

  def siekiUkemi = new IchidanVerb(naiStem.obj.add("される"))

  def you = Terminal(stemWithMap(GodanMappings.uo).add("う"))

  def meirei = MeireiForm(kateiStem.obj)
}

class Iku(obj: ConjObject) extends GodanVerb(obj) {
  override def taStem = TaStem(obj.without("く").add("っ"))
}
