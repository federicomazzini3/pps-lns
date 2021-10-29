package lns.core

import indigo.shared.Outcome
import lns.subsystems.prolog.{ Prolog, PrologCommand, Query }
import lns.subsystems.prolog.SessionId
import lns.subsystems.prolog.QueryId

case class PrologSessionState(val session: SessionId, val query: Option[QueryId])

/**
 * Prolog service client to easily send PrologCommand
 * @param state
 */
case class PrologClient(val state: Option[PrologSessionState] = None) {

  def consultDone: Boolean = state.nonEmpty

  def hasQuery(queryId: QueryId): Boolean = state match {
    case (Some(s)) if s.query.nonEmpty => s.query.get == queryId
    case _                             => false
  }

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
