package lns.subsystems.prolog

import indigo.*

import java.util.UUID
import scala.collection.immutable.{ HashMap, Queue }

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
}

/**
 * Represents an atom Term in Prolog
 * @param id
 *   identifier as a string
 */
case class Atom(val id: String) extends Term {
  override def toString: String = id
}

given Conversion[String, Atom] with
  def apply(s: String): Atom = Atom(s)

/**
 * Represents numbers (integer or float) in Prolog.
 * @param value
 *   float number
 * @param isFloat
 */
case class Num(val value: Float, val isFloat: Boolean) extends Term {
  override def toString: String = isFloat match {
    case true => value.toString
    case _    => value.toInt.toString
  }
}

/**
 * Represents a logical variables in Prolog
 * @param id
 *   identifier as a string
 */
case class Var(val id: String) extends Term {
  override def toString: String = id
}

/**
 * Represents atoms and compound terms in Prolog
 * @param id
 *   identifier as a string
 * @param args
 *   arguments if term is compound
 */
case class Compound(val atom: Atom, val args: Term*) extends Term {
  override def toString: String = atom.toString + "(" + args.toString + ")";
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

// TODO: Pattern strategy -> factories as functions
//  (theory: String, query: Option[Query] = None) => AsyncSession
trait AsyncSessionFactory {
  def create(theory: String, query: Option[Query] = None): AsyncSession
}

/**
 * An Indigo SubSystem that represent a prolog query service. Like an autonomous actor intercepting messages/events of
 * type PrologCommand on every game loop cycle. Also, every FrameTick it checks every AsyncSession for an answer result
 * (pooling mode)
 * @param sessionFactory
 *   used to create an AsyncSession
 */
case class PrologService(sessionFactory: AsyncSessionFactory) extends SubSystem:
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
      Outcome(model + (sessionId -> sessionFactory.create(theory, query)))

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
