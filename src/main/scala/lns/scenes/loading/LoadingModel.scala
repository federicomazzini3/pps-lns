package lns.scenes.loading

/**
 * A Loading Model
 */
sealed trait LoadingModel{
  
  /**
   * @return the [[LoadingState]] of a [[LoadingModel]]
   */
  def loadingState: LoadingState
}

object LoadingModel {
  val initial: LoadingModel = LoadingModelImpl(LoadingState.NotStarted)

  def inProgress(percent: Int): LoadingModel = LoadingModelImpl(LoadingState.InProgress(percent))

  val complete: LoadingModel = LoadingModelImpl(LoadingState.Complete)

  val error: LoadingModel = LoadingModelImpl(LoadingState.Error)

  private case class LoadingModelImpl(loadingState: LoadingState) extends LoadingModel
}

sealed trait LoadingState
object LoadingState {
  case object NotStarted                    extends LoadingState
  final case class InProgress(percent: Int) extends LoadingState
  case object Complete                      extends LoadingState
  case object Error                         extends LoadingState
}