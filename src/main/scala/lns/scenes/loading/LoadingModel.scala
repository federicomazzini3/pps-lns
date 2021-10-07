package lns.scenes.loading

/**
  * A Loading Model
  */
sealed trait LoadingModel

object LoadingModel {
  val initial: LoadingModel = NotStarted
  case object NotStarted                    extends LoadingModel
  final case class InProgress(percent: Int) extends LoadingModel
  case object Complete                      extends LoadingModel
  case object Error                         extends LoadingModel
}
