package lns.subsystems.prolog

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

/**
 * Loads tau-prolog engine on the client browser using ScalaJS dom extension
 */
def loadTauProlog(): Unit = {
  import org.scalajs.dom
  import org.scalajs.dom.document
  import org.scalajs.dom.html.Script

  val prologScript: Script = document.createElement("SCRIPT").asInstanceOf[Script]
  prologScript.src = "assets/prolog/tau-prolog.js"
  document.head.appendChild(prologScript)
}

@js.native
@JSGlobal("pl")
object TauProlog extends js.Object {
  def create(): TauSession = js.native
}

@js.native
@JSGlobal("pl.type.Session")
class TauSession extends js.Object {
  def consult(theory: String, option: js.Function0[Unit]): Unit = js.native
  def query(goal: String, option: js.Function0[Unit]): Unit     = js.native
  def answer(option: js.Function1[TauSubstitution, Unit]): Unit = js.native
}

@js.native
@JSGlobal("pl.type.Num")
class TauNum(val value: Float, val is_float: Boolean) extends js.Object {}

@js.native
@JSGlobal("pl.type.Var")
class TauVar(val id: String) extends js.Object {}

@js.native
@JSGlobal("pl.type.Term")
class TauTerm(val id: String, val args: js.Array[TauTerm | TauNum | TauVar]) extends js.Object {}

@js.native
@JSGlobal("pl.type.Substitution")
class TauSubstitution(val links: js.Dictionary[TauTerm]) extends js.Object {}
