package ws.eiennohito.utils

import scala.reflect.macros.blackbox

/**
 * @author eiennohito
 * @since 15/08/16
 */
object ImplicitClassloaders {
  def macroImpl(c: blackbox.Context): c.Expr[ClassLoader] = {
    import c.universe._

    c.Expr(q"this.getClass().getClassloader()")
  }
}
