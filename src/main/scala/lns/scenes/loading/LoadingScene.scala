package lns.scenes.loading

import indigo.*
import indigo.scenes.*
import lns.StartupData
import lns.core.{EmptyScene, Model, ViewModel}

final case class LoadingScene() extends EmptyScene {
  type SceneModel     = LoadingModel
  type SceneViewModel = Unit

  def name: SceneName = LoadingScene.name

  def modelLens: Lens[Model,SceneModel] = Lens(m => m.loading, (m, sm) => m.copy(loading = sm))
  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(_ => (), (vm, _) => vm)
}

object LoadingScene {
  val name: SceneName = SceneName("demo")
}
