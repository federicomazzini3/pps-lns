package lns.scenes.loading

import lns.core.ScalaPrologSession

/**
 * A Loading Model
 */
sealed trait LoadingModel
sealed trait PrologLoading extends LoadingModel {
  val session: ScalaPrologSession
}

object LoadingModel {
  val initial: LoadingModel = NotStarted
  case object NotStarted                                           extends LoadingModel
  final case class InProgress(percent: Int)                        extends LoadingModel
  final case class AwaitPrologConsult(session: ScalaPrologSession) extends PrologLoading
  final case class AwaitPrologQuery(session: ScalaPrologSession)   extends PrologLoading
  final case class AwaitPrologAnswer(session: ScalaPrologSession)  extends PrologLoading
  case object Complete                                             extends LoadingModel
  case object Error                                                extends LoadingModel
}
