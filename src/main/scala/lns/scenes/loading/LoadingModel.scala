package lns.scenes.loading

sealed trait LoadingModel

object LoadingModel {
  val initial: LoadingModel = LoadingModelImpl()

  private case class LoadingModelImpl() extends LoadingModel
}
