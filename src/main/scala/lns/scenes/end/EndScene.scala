package lns.scenes.end

import indigo.*
import indigo.scenes.*
import indigo.scenes.SceneEvent.JumpTo
import indigo.shared.events.FrameTick
import lns.StartupData
import lns.scenes.end.*
import lns.core.{ EmptyScene, Model, ViewModel }
import lns.scenes.game.GameScene
import lns.scenes.game.subsystems.ResetSubsystem
import lns.scenes.loading.LoadingScene
import lns.scenes.menu.StartEvent

import scala.language.implicitConversions

final case class EndScene() extends EmptyScene {
  type SceneModel     = EndModel
  type SceneViewModel = EndViewModel

  def name: SceneName = EndScene.name

  def modelLens: Lens[Model, SceneModel]             = Lens(m => m.end, (m, sm) => m.copy(end = sm))
  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(vm => vm.end, (vm, svm) => vm.copy(end = svm))

  override def updateModel(
      context: FrameContext[StartupData],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {
    case Restart | KeyboardEvent.KeyUp(Key.SPACE) =>
      model.addGlobalEvents(JumpTo(GameScene.name)).addGlobalEvents(Restart)

    case _ =>
      model
  }

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

  override def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    EndView
      .draw(
        context.startUpData.screenDimensions,
        viewModel.button
      )
}

object EndScene {
  val name: SceneName = SceneName("end")
}
