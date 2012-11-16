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

case class TaForm(obj: ConjObject) extends Renderable with Chaining[TaForm]


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

case class AdjStem(obj: ConjObject) extends Renderable {

}

trait Adjective extends Renderable with Chaining[Adjective]

case class AdjI (obj: ConjObject) extends Adjective
case class Nai(obj: ConjObject) extends Adjective

case class NaiStem(obj: ConjObject) extends Renderable with Chaining[NaiStem] {
  def nai: Adjective = Nai(obj.add("ない"))

  override protected def rules = make(_.nai, "nai") :: Nil
}

case class MasuStem(obj: ConjObject) extends Renderable {
  def masu = new GodanVerb(obj.add("ます"))
  def tai = new AdjI(obj.add("たい"))
}

case class ConjRule[T <: Renderable](tf: T => Renderable, chain: List[String]) {
  def chain[U <: Renderable](ctf: U => T, name: String): ConjRule[U] = ConjRule(tf.compose(ctf), name :: chain)
}




trait Chaining[T <: Renderable] extends Renderable { self : T =>
  type Generator = Int => List[ConjRule[T]]

  protected def rules: List[Generator] = Nil
  def generate(depth: Int): List[ConjRule[T]] = {
    assert(depth >= 0, "Depth should be non-negative")
    if (depth == 0) {
      return new ConjRule[T](x => x, Nil) :: Nil
    }
    rules flatMap (_(depth))
  }

  // hides T => U composition
  def make[U <: Chaining[U]](rule: T => U, name: String): Generator = {
    def generate(lvl: Int): List[ConjRule[T]] = {
      val u = rule(self)
      u.generate(lvl - 1).map(_.chain(rule, name))
    }
    generate
  }

  implicit def tuple2Generator[U <: Chaining[U]](rule: (T => U, String)) =
    make(rule._1, rule._2)
}

trait Verb extends Renderable with Chaining[Verb] {
  def taStem: TaStem
  def naiStem: NaiStem
  def masuStem: MasuStem

  //shortcuts
  def past = taStem ta
  def teForm = taStem te

  override protected def rules = make(_.naiStem, "naiStem") :: Nil
}

object Verb {
  def dummy = new GodanVerb(ConjObject.Empty)
  def godan(s: String) = new GodanVerb(new ConjObject(s :: Nil))
  def ichidan(s: String) = new IchidanVerb(new ConjObject(s :: Nil))
}

case class BaIf(obj: ConjObject) extends Renderable {}

case class KateiStem(obj: ConjObject) extends Renderable {
  def ba = BaIf(obj.add("ば"))
}

class IchidanVerb(protected val obj: ConjObject) extends Verb {
  def stem = obj.without("る")
  def taStem = TaStem(stem)
  def naiStem = NaiStem(stem)
  def masuStem = MasuStem(stem)
  def kateiStem = KateiStem(stem.add("れ"))
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

  def naiStem = {
    obj.last(1) match {
      case Some("う") => new NaiStem(obj.without("う").add("わ"))
      case Some(c) => {
        val a = (c(0) - 2).toChar //a i u e o
        new NaiStem(obj.without(c).add(a.toString))
      }
      case _ => new NaiStem(ConjObject.Empty)
    }
  }

  def masuStem = {
    obj.last(1) match {
      case Some(c) => {
        val a = (c(0) - 1).toChar //a i u e o
        new MasuStem(obj.without(c).add(a.toString))
      }
      case _ => new MasuStem(ConjObject.Empty)
    }
  }

  def kateiStem = {
    obj.last(1) match {
      case Some(c) => {
        val a = (c(0) + 1).toChar //a i u e o
        new KateiStem(obj.without(c).add(a.toString))
      }
      case _ => new KateiStem(ConjObject.Empty)
    }
  }

}
