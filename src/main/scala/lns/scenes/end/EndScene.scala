package lns.scenes.end

import indigo.*
import indigo.scenes.*
import lns.StartupData
import lns.core.{EmptyScene, Model, ViewModel}

final case class EndScene() extends EmptyScene {
  type SceneModel     = EndModel
  type SceneViewModel = Unit

  def name: SceneName = SceneName("end")

  def modelLens: Lens[Model,SceneModel] = Lens(m => m.end, (m, sm) => m.copy(end = sm))
  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(_ => (), (vm, _) => vm)
}
