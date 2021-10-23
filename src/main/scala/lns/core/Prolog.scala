package lns.core

import scala.scalajs.js
import scala.scalajs.js.annotation.JSGlobal

case class ScalaPrologSession() {
  val session: PrologSession = Prolog.create()

  var consultResult: Option[Boolean]     = None
  var queryResult: Option[Boolean]       = None
  var answerResult: Option[Substitution] = None

  def consult(theory: String): Unit = session.consult(theory, () => consultResult = Some(true))
  def query(goal: String): Unit     = session.query(goal, () => queryResult = Some(true))
  def answer(): Unit                = session.answer((sub: Substitution) => answerResult = Some(sub))

}

@js.native
@JSGlobal("pl")
object Prolog extends js.Object {
  def create(): PrologSession = js.native
}

@js.native
@JSGlobal("pl.type.Session")
class PrologSession extends js.Object {
  def consult(theory: String, option: js.Function0[Unit]): Unit = js.native
  def query(goal: String, option: js.Function0[Unit]): Unit     = js.native
  def answer(option: js.Function1[Substitution, Unit]): Unit    = js.native
}

@js.native
@JSGlobal("pl.type.Substitution")
class Substitution(val links: js.Dictionary[Term]) extends js.Object {}

@js.native
@JSGlobal("pl.type.Term")
class Term(val id: String, val args: js.Array[Term]) extends js.Object {}
