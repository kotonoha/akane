package ws.eiennohito.utils

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

/**
 * @author eiennohito
 * @since 15/08/14
 */

object Foreach {
  //highly experimental! do not touch!
  def fori(start: Int, end: Int) (fn: Int => Unit): Unit = macro ForeachImpl.fori_impl
}

object ForeachImpl {
  def fori_impl(c: blackbox.Context)(start: c.Expr[Int], end: c.Expr[Int])(fn: c.Expr[Int => Unit]): c.Expr[Unit] = {
    import c.universe._

    //println(showRaw(fn))

    import org.scalamacros.resetallattrs._

    val body = fn.tree match {
      case q"{($v=>$body)}" =>
        val endName = TermName("__end_" + c.enclosingPosition.line + "_" + c.enclosingPosition.column + "_$internal")
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

