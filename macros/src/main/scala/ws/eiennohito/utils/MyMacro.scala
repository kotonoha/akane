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

package ws.eiennohito.utils

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/**
  * @author eiennohito
  * @since 15/08/14
  */
object Foreach {
  //highly experimental! do not touch!
  def fori(start: Int, end: Int)(fn: Int => Unit): Unit = macro ForeachImpl.fori_impl
}

object ForeachImpl {
  def fori_impl(c: blackbox.Context)(start: c.Expr[Int], end: c.Expr[Int])(
      fn: c.Expr[Int => Unit]): c.Expr[Unit] = {
    import c.universe._

    //println(showRaw(fn))

    import org.scalamacros.resetallattrs._

    val body = fn.tree match {
      case q"{($v=>$body)}" =>
        val endName = TermName(
          "__end_" + c.enclosingPosition.line + "_" + c.enclosingPosition.column + "_$internal")
        val q"$_ val $name: $tp = $smt" = v
        q"""
           var $name: $tp = $start;
           val $endName: $tp = $end;
           while($name < $endName) {
             ${c.resetAllAttrs(body)}
             $name += 1;
           }
         """
      case _ => c.abort(c.enclosingPosition, s"invalid body expression, $fn")
    }

    //println(showRaw(body))
    //println(showCode(body))

    c.Expr[Unit](q"{$body}")
  }
}
