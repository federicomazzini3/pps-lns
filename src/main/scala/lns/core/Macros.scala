package lns.core

import scala.quoted.*

object Macros {

  /**
   * Macro to call the method copy of a case class from inside it using contex arguments as named parameters, avoiding
   * boilerplate code
   *
   * Eg. from: def edit(x,y,z) = copy(x=x,y=y,z=z); to: def edit(x,y,z) = copyMacro
   *
   * @tparam T
   *   type of case class
   * @return
   *   copy call return
   */
  inline def copyMacro[T]: T = ${ copyMacroImpl[T] }

  /**
   * Find the "this" Term that invoked the macro going up on the Typed Abstract Syntax Tree
   */
  def resolveThis(using quotes: Quotes): quotes.reflect.Term =
    import quotes.reflect.*

    var sym = Symbol.spliceOwner
    while sym != null && !sym.isClassDef do sym = sym.owner
    This(sym)

  /**
   * The copyMacro implementation
   * @param quotes
   *   quotation context provided by a macro expansion, it contains the low-level Typed AST API metaprogramming API
   * @param tpe
   *   current Type[T] needed contextually because using T in a quoted expression
   * @tparam T
   *   type of case class
   * @return
   *   a quoted expression of type T representing the copy method call
   */
  def copyMacroImpl[T](using quotes: Quotes, tpe: Type[T]): Expr[T] = {
    import quotes.reflect.*

    /*
    Scala 3 Macro & Reflection API is currently undocumented, so I'll explain every step
     */

    val callee      = Symbol.spliceOwner.owner                                   // current macro invocator method
    val termParamss = callee.paramSymss.filterNot(_.headOption.exists(_.isType)) // method arguments skipping type pars
    val refss       = termParamss.map(_.map(Ref.apply))                          // each argument Symbol to Ref

    if refss.length > 1 then report.throwError(s"callee $callee has curried parameter lists.")

    val args = refss.headOption.toList.flatten.map(a => NamedArg(a.show, a)) // List of NamedArg from arg Ref

    val tree = Select.overloaded( // calling case class method "copy" selecting the right overloaded method by arguments
      resolveThis,
      "copy",
      List(),
      args
    ); // Alternative would be Apply(Select.unique(resolveThis, "copy"), args) for calling non overloaded methods

    tree.asExprOf[T] // casting quoted Expr type to T
  }
}
