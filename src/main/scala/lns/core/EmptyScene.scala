package lns.core

import indigo.*
import indigo.scenes.*
import lns.StartupData

/**
 * An empty scene to be extended
 */
abstract class EmptyScene() extends Scene[StartupData, Model, ViewModel] {

  def eventFilters: EventFilters = EventFilters.Restricted
  def subSystems: Set[SubSystem] = Set()

  def updateModel (context: FrameContext[StartupData], model: SceneModel ): GlobalEvent => Outcome[SceneModel] = _ => Outcome(model)
  def updateViewModel (context: FrameContext[StartupData], model: SceneModel , viewModel: SceneViewModel): GlobalEvent => Outcome[SceneViewModel] = _ => Outcome(viewModel)

  def present (context: FrameContext[StartupData], model: SceneModel , viewModel: SceneViewModel): Outcome[SceneUpdateFragment] = Outcome(SceneUpdateFragment.empty)
}
