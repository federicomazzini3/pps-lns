package lns.scenes.loading

import indigo._
import indigo.scenes._
import indigoextras.subsystems._
import indigo.scenes.SceneEvent.JumpTo
import lns.StartupData
import lns.core.{Assets, EmptyScene, Model, ViewModel}
import lns.scenes.game.GameScene
import lns.scenes.loading.LoadingModel

final case class LoadingScene() extends EmptyScene {
  type SceneModel     = LoadingModel
  type SceneViewModel = Unit

  def name: SceneName = LoadingScene.name

  def modelLens: Lens[Model, SceneModel] = Lens(m => m.loading, (m, sm) => m.copy(loading = sm))

  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(_ => (), (vm, _) => vm)

  override def subSystems: Set[SubSystem] = Set(AssetBundleLoader)

  override def updateModel(
    context: FrameContext[StartupData],
    loading: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {
    case FrameTick =>
      loading match {
        case LoadingModel.NotStarted => {
          Outcome(LoadingModel.InProgress(0))
            .addGlobalEvents(
              AssetBundleLoaderEvent.Load(BindingKey("Loading"), Assets.secondary())
            )
        }

        case _ =>
          Outcome(loading)
      }

    case AssetBundleLoaderEvent.LoadProgress(_, percent, _, _) =>
      Outcome(LoadingModel.InProgress(percent))

    case AssetBundleLoaderEvent.Success(_) =>
      Outcome(LoadingModel.Complete)
    //.addGlobalEvents(JumpTo(GameScene.name))

    case AssetBundleLoaderEvent.Failure(_, _) =>
      Outcome(LoadingModel.Error)

    case _ =>
      Outcome(loading)
  }

  override def present(
    context: FrameContext[StartupData],
    loading: SceneModel,
    viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    Outcome(
      LoadingView.draw(
        loading,
        context.startUpData.screenDimensions
      )
    )

}

object LoadingScene {
  val name: SceneName = SceneName("demo")
}
