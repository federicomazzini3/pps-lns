package lns.scenes.menu

import indigo.*
import indigo.scenes.*
import indigo.scenes.SceneEvent.JumpTo
import indigo.shared.events.FrameTick
import indigo.shared.scenegraph.Text
import lns.StartupData
import lns.core.{ Assets, EmptyScene, Model, ViewModel }
import lns.scenes.loading.LoadingScene
import lns.scenes.game.|+|

import scala.language.implicitConversions

case object StartEvent extends GlobalEvent

final case class MenuScene() extends EmptyScene {
  type SceneModel     = Unit
  type SceneViewModel = MenuViewModel

  def name: SceneName = SceneName("menu")

  def modelLens: Lens[Model, SceneModel]             = Lens.unit
  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(vm => vm.menu, (vm, svm) => vm.copy(menu = svm))

  override def updateViewModel(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel] = {
    case FrameTick =>
      viewModel.button.update(context.inputState.mouse).map { btn =>
        viewModel.copy(button = btn)
      }

    case _ =>
      viewModel
  }

  override def updateModel(
      context: FrameContext[StartupData],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {
    case StartEvent | KeyboardEvent.KeyUp(Key.SPACE) =>
      model.addGlobalEvents(JumpTo(LoadingScene.name))

    case _ =>
      model
  }

  override def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    SceneUpdateFragment(
      Group(viewModel.button.draw) |+| Group(
        Text(
          "use A-W-S-D to move and arrow keys to shot",
          context.startUpData.screenDimensions.horizontalCenter,
          context.startUpData.screenDimensions.height / 4 * 3,
          1,
          Assets.Fonts.fontKey,
          Assets.Fonts.fontMaterial
        ).alignCenter.withScale(Vector2(2, 2))
      ) |+| Group(
        Text(
          "Lost n Souls",
          context.startUpData.screenDimensions.horizontalCenter,
          context.startUpData.screenDimensions.height / 5,
          1,
          Assets.Fonts.fontKey,
          Assets.Fonts.fontMaterial
        ).alignCenter.withScale(Vector2(8, 8))
      )
    )
}
