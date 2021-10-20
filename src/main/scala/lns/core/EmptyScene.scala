package lns.core

import indigo.*
import indigo.scenes.*
import lns.StartupData
import scala.language.implicitConversions

/**
 * An empty scene to be extended
 */
abstract class EmptyScene() extends Scene[StartupData, Model, ViewModel] {

  def eventFilters: EventFilters = EventFilters.Restricted
  def subSystems: Set[SubSystem] = Set()

  given Conversion[SceneModel, Outcome[SceneModel]] with
    def apply(model: SceneModel): Outcome[SceneModel] = Outcome(model)

  given Conversion[SceneViewModel, Outcome[SceneViewModel]] with
    def apply(viewModel: SceneViewModel): Outcome[SceneViewModel] = Outcome(viewModel)

  given Conversion[SceneUpdateFragment, Outcome[SceneUpdateFragment]] with
    def apply(sceneUpdateFragment: SceneUpdateFragment): Outcome[SceneUpdateFragment] = Outcome(sceneUpdateFragment)

  def updateModel(context: FrameContext[StartupData], model: SceneModel): GlobalEvent => Outcome[SceneModel] =
    _ => model

  def updateViewModel(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel] = _ => viewModel

  def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] = SceneUpdateFragment.empty
}
