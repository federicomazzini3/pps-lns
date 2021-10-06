package lns.scenes.menu

import indigo.*
import indigo.scenes.SceneEvent.JumpTo
import indigo.shared.events.FrameTick
import indigo.scenes.*
import lns.StartupData
import lns.core.{EmptyScene, Model, ViewModel}
import lns.scenes.loading.LoadingScene

final case class MenuScene() extends EmptyScene {
  type SceneModel     = Unit
  type SceneViewModel = MenuViewModel

  def name: SceneName = SceneName("menu")

  def modelLens: Lens[Model,SceneModel] = Lens.unit
  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(vm => vm.menu, (vm, svm) => vm.copy(menu = svm))

  override def updateViewModel (context: FrameContext[StartupData], model: SceneModel , viewModel: SceneViewModel): GlobalEvent => Outcome[SceneViewModel] = e => e match {
      case FrameTick =>
        viewModel.button.update(context.inputState.mouse).map { btn =>
          viewModel.copy(button = btn)
        }

      case _ =>
        Outcome(viewModel)
    }

  override def updateModel (context: FrameContext[StartupData], model: SceneModel ): GlobalEvent => Outcome[SceneModel] = e => e match {
      case StartEvent =>
        Outcome(model).addGlobalEvents(JumpTo(LoadingScene.name))

      case _ =>
        Outcome(model)
    }

  override def present(context: FrameContext[StartupData], model: SceneModel, viewModel: SceneViewModel): Outcome[SceneUpdateFragment] =
    Outcome(SceneUpdateFragment(viewModel.button.draw))
}
