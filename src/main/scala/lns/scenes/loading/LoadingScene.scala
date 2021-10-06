package lns.scenes.loading

import indigo._
import indigo.scenes._
import indigoextras.subsystems._
import indigo.scenes.SceneEvent.JumpTo
import lns.StartupData
import lns.core.{Assets, EmptyScene, Model, ViewModel}
import lns.scenes.game.GameScene

final case class LoadingScene() extends EmptyScene {
  type SceneModel = LoadingModel
  type SceneViewModel = Unit

  def name: SceneName = SceneName("loading")

  def modelLens: Lens[Model, SceneModel] = Lens(
    m => m.loading, (m, sm) => m.copy(loading = sm))

  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(_ => (), (vm, _) => vm)

  override def subSystems: Set[SubSystem] = Set(AssetBundleLoader)

  override def updateModel(context: FrameContext[StartupData], model: SceneModel): GlobalEvent => Outcome[SceneModel] = {
    case FrameTick =>
      model.loadingState match {
        case LoadingState.NotStarted => {
          Outcome(LoadingModel.inProgress(0))
            .addGlobalEvents(
              AssetBundleLoaderEvent.Load(BindingKey("Loading"), Assets.secondary())
            )
        }

        case _ =>
          Outcome(model)
      }

    case AssetBundleLoaderEvent.LoadProgress(_, percent, _, _) =>
      Outcome(LoadingModel.inProgress(percent))

    case AssetBundleLoaderEvent.Success(_) =>
      Outcome(LoadingModel.complete)
    //.addGlobalEvents(JumpTo(GameScene.name))

    case AssetBundleLoaderEvent.Failure(_, _) =>
      Outcome(LoadingModel.error)

    case _ =>
      Outcome(model)
  }

  override def present(
                        context: FrameContext[StartupData],
                        model: SceneModel,
                        viewModel: SceneViewModel): Outcome[SceneUpdateFragment] =
  Outcome(
    LoadingView.draw(
      model.loadingState
    )
  )

}
