package lns.core

import indigo.shared.Outcome
import lns.subsystems.prolog.{ Prolog, PrologCommand, Query }
import lns.subsystems.prolog.SessionId
import lns.subsystems.prolog.QueryId

/**
 * A prolog client session data
 * @param session
 *   unique session identifier
 * @param query
 *   optional unique query identifier
 */
case class PrologSessionState(val session: SessionId, val query: Option[QueryId])

/**
 * Prolog service client to easily send PrologCommand
 * @param state
 *   optional [[PrologSessionState]] to provide current client state about the last consult done
 */
case class PrologClient(val state: Option[PrologSessionState] = None) {

  /**
   * @return
   *   True if the client has executed a consult to the service
   */
  def consultDone: Boolean = state.nonEmpty

  /**
   * Checks if the consult was sent to the service with a specific queryId
   * @param queryId
   *   the query identifier to check
   * @return
   *   True if the queryId provided match the consult queryId
   */
  def hasQuery(queryId: QueryId): Boolean = state match {
    case (Some(s)) if s.query.nonEmpty => s.query.get == queryId
    case _                             => false
  }

  /**
   * Send a PrologCommand.Consult to the prolog service
   * @param theory
   *   the prolog theory to consult
   * @param goal
   *   a first goal to be executed
   * @return
   *   a new [[PrologClient]] with a new [[PrologSessionState]] to store the new sessionId and queryId
   */
  def consult(theory: String, goal: String): Outcome[PrologClient] = {
    val sessionId = Prolog.newSessionId
    val queryId   = Prolog.newQueryId

    Outcome(PrologClient(Some(PrologSessionState(sessionId, Some(queryId))))).addGlobalEvents(
      PrologCommand.Consult(
        sessionId,
        theory,
        Some(Query(queryId, goal))
      )
    )
  }
}
