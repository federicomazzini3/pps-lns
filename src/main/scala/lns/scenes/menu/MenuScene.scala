package lns.scenes.menu

import indigo.*
import indigo.scenes.*
import lns.StartupData
import lns.core.{EmptyScene, Model, ViewModel}

final case class MenuScene() extends EmptyScene {
  type SceneModel     = MenuModel
  type SceneViewModel = Unit

  def name: SceneName = SceneName("menu")

  def modelLens: Lens[Model,SceneModel] = Lens(m => m.menu, (m, sm) => m.copy(menu = sm))
  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(_ => (), (vm, _) => vm)

}
