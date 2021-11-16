package lns.subsystems.prolog

import scala.collection.immutable.{ HashMap, Queue }
import scala.language.implicitConversions
import scala.scalajs.js
import scala.scalajs.js.Dictionary
import Term.*

given Conversion[TauNum, Num] with
  def apply(n: TauNum): Num = Num(n.value, n.is_float)

given Conversion[TauVar, Var] with
  def apply(v: TauVar): Var = Var(v.id)

given Conversion[TauTerm, Term] with
  def apply(t: TauTerm): Term = t.args.length match {
    case 0 => Atom(t.id)
    case _ =>
      Struct(
        Atom(t.id),
        t.args
          .map[Term](arg =>
            arg match {
              case a: TauNum  => a
              case a: TauVar  => a
              case a: TauTerm => a
            }
          )
          .toList: _*
      )
  }

given Conversion[TauSubstitution, Substitution] with
  def apply(t: TauSubstitution): Substitution = Substitution(
    t.links.foldLeft(HashMap[String, Term]())((hmap, kv) => hmap + (kv._1 -> kv._2))
  )

/**
 * A TauProlog client designed to manage its session and handle its callbacks using mutable variables. We need
 * mutability here because the Javascript event loop calls the callbacks providing results to store, all indipendently
 * of Indigo game loop. At the moment there is no way to create and send an Indigo event inside a callback execution to
 * manage a result in a immutable mode
 * @param theory
 *   the Prolog program theory
 * @param query
 *   an optional query to request after theory is parsed
 */
case class TauClient(theory: String, query: Option[Query] = None) extends AsyncSession {
  private var consultResult: Option[Boolean]                    = None
  private var answersResults: Map[QueryId, Queue[Substitution]] = HashMap()

  private val session: TauSession = createTauSession()
  private val consult: Unit =
    session.consult(
      theory,
      () => {
        consultResult = Some(true); query.map(q => doQuery(q))
      }
    )

  private def createTauSession(): TauSession = TauProlog.create()

  def doQuery(query: Query): Unit = consultResult match {
    case Some(true) =>
      answersResults = answersResults + (query.id -> Queue());
      session.query(query.goal, () => askAnswer(query.id))
    case _ => ()
  }

  def askAnswer(queryId: QueryId): Unit = answersResults
    .get(queryId)
    .map(queryAnswersResults =>
      session.answer { (sub: TauSubstitution) =>
        answersResults = answersResults + (queryId -> (queryAnswersResults :+ sub))
      }
    )

  def getAnswerResult(queryId: QueryId): Option[Substitution] = for {
    queryAnswersResults <- answersResults.get(queryId)
    if !queryAnswersResults.isEmpty
    (result, newQueue) = queryAnswersResults.dequeue
  } yield {
    answersResults = answersResults + (queryId -> newQueue); result
  }

  def getAllAnswersResults: List[(QueryId, Substitution)] = for {
    queryId <- answersResults.keys.toList
    result  <- getAnswerResult(queryId)
  } yield (queryId, result)

}

object TauClientFactory extends AsyncSessionFactory {
  def create(theory: String, query: Option[Query] = None): TauClient = TauClient(theory, query)
}
