package lns.subsystems.prolog

import indigo.*

import java.util.UUID
import scala.collection.immutable.{ HashMap, Queue }

import scala.language.implicitConversions

type SessionId = String
type QueryId   = String

object Prolog {
  def newSessionId: SessionId = UUID.randomUUID().toString
  def newQueryId: QueryId     = UUID.randomUUID().toString
}

/**
 * Represent a prolog query
 * @param id
 *   unique identifier
 * @param goal
 *   prolog goal
 */
case class Query(id: QueryId, goal: String)

/**
 * Represents every term in Prolog
 */
trait Term {
  def toString: String
  def toList: List[Term]
}

object Term {

  /**
   * Represents an atom Term in Prolog
   * @param id
   *   identifier as a string
   */
  case class Atom(val id: String) extends TermImplementation

  given Conversion[String, Atom] with
    def apply(s: String): Atom = Atom(s)

  /**
   * Represents numbers (integer or float) in Prolog.
   * @param value
   *   float number
   * @param isFloat
   */
  case class Num(val value: Float, val isFloat: Boolean) extends TermImplementation

  /**
   * Represents a logical variables in Prolog
   * @param id
   *   identifier as a string
   */
  case class Var(val id: String) extends TermImplementation

  /**
   * Represents atoms and compound terms in Prolog
   * @param id
   *   identifier as a string
   * @param args
   *   arguments if term is compound
   */
  case class Struct(val atom: Atom, val args: Term*) extends TermImplementation

  sealed trait TermImplementation extends Term {
    override def toString: String = this match {
      case Atom(id)            => id
      case Var(id)             => id
      case Num(value, true)    => value.toString
      case Num(value, _)       => value.toInt.toString
      case Struct(atom, args*) => atom.toString + "(" + args.mkString(", ") + ")";
    }

    override def toList: List[Term] = this match {
      case Struct(Atom("."), h, t @ Struct(Atom("."), _, _)) => h :: t
      case Struct(Atom("."), h, Atom("[]"))                  => h :: Nil
      case _                                                 => Nil
    }
  }

  implicit def toList(t: Term): List[Term] = t.toList

}

/**
 * Represents the substitutions in the answers after the resolution process
 * @param links
 *   map of
 */
case class Substitution(val links: Map[String, Term])

/**
 * Represents an interface to an async prolog engine. Since Indigo runs on Javascript but can't handle async callbacks
 * to manage an external Prolog engine (remote service or javascript), we need an autonomous object to collect the query
 * results
 */
trait AsyncSession {
  val theory: String
  val query: Option[Query]

  /**
   * send a query goal to the prolog engine
   * @param query
   */
  def doQuery(query: Query): Unit

  /**
   * send an answer request to the prolog engine
   * @param queryId
   *   the identifier of the query/goal to be answered
   */
  def askAnswer(queryId: QueryId): Unit

  /**
   * get first answer result for a single query - then discard it
   * @param queryId
   *   the identifier of the query/goal
   * @return
   *   if there is a result, a Substitution object
   */
  def getAnswerResult(queryId: QueryId): Option[Substitution]

  /**
   * get for each query done the first answer result if exists - then discard all
   * @return
   *   a list of tuples representing for each queryId a Substitution object
   */
  def getAllAnswersResults: List[(QueryId, Substitution)]
}

/**
 * An Indigo SubSystem that represent a prolog query service. Like an autonomous actor intercepting messages/events of
 * type PrologCommand on every game loop cycle. Also, every FrameTick it checks every AsyncSession for an answer result
 * (pooling mode)
 * @param sessionFactory
 *   used to create an AsyncSession
 */
case class PrologService(sessionFactory: (theory: String, query: Option[Query]) => AsyncSession) extends SubSystem:
  type EventType      = GlobalEvent
  type SubSystemModel = Map[SessionId, AsyncSession]

  val eventFilter: GlobalEvent => Option[EventType] = {
    case e: PrologCommand => Option(e)
    case FrameTick        => Option(FrameTick)
    case _                => None
  }

  def initialModel: Outcome[SubSystemModel] =
    Outcome(HashMap[SessionId, AsyncSession]())

  def update(context: SubSystemFrameContext, model: SubSystemModel): GlobalEvent => Outcome[SubSystemModel] = {
    case PrologCommand.Consult(sessionId, theory, query) if !model.isDefinedAt(sessionId) =>
      Outcome(model + (sessionId -> sessionFactory(theory, query)))

    case FrameTick =>
      val newAnswerEvents = for {
        session                 <- model.values.toList
        (queryId, substitution) <- session.getAllAnswersResults
      } yield PrologEvent.Answer(queryId, substitution)

      Outcome(model).addGlobalEvents(newAnswerEvents)

    case _ => Outcome(model)
  }

  def present(context: SubSystemFrameContext, model: SubSystemModel): Outcome[SceneUpdateFragment] = Outcome(
    SceneUpdateFragment.empty
  )

enum PrologCommand extends GlobalEvent:
  case Consult(sessionId: SessionId, theory: String, query: Option[Query]) extends PrologCommand
  case DoQuery(sessionId: SessionId, query: Query) extends PrologCommand
  case AskAnswer(sessionId: SessionId, queryId: QueryId) extends PrologCommand

enum PrologEvent extends GlobalEvent:
  case Answer(queryId: QueryId, sub: Substitution) extends PrologEvent
