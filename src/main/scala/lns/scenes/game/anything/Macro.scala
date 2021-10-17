package lns.scenes.game.anything

import scala.quoted.*

object Macros {
  inline def copyMacro[T]: T = ${ copyMacroImpl[T] }

  def resolveThis(using Quotes): quotes.reflect.Term =
    import quotes.reflect.*
    var sym = Symbol.spliceOwner
    while sym != null && !sym.isClassDef do sym = sym.owner
    This(sym)

  def copyMacroImpl[T](using quotes: Quotes, tpe: Type[T]): Expr[T] = {
    import quotes.reflect.*

    val callee      = Symbol.spliceOwner.owner
    val termParamss = callee.paramSymss.filterNot(_.headOption.exists(_.isType))
    val refss       = termParamss.map(_.map(Ref.apply))

    if refss.length > 1 then report.throwError(s"callee $callee has curried parameter lists.")

    val args = refss.headOption.toList.flatten.map { a =>
      NamedArg(a.show, a)
    }

    val tree = Select.overloaded(
      resolveThis,
      "copy",
      List(),
      args
    ); // Apply(Select.unique(resolveThis, "copy"), args)

    tree.asExprOf[T]
  }
}
