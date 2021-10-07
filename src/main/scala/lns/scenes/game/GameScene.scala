package lns.scenes.game

import indigo.*
import indigo.scenes.*
import lns.StartupData
import lns.core.{ EmptyScene, Model, ViewModel }
import scala.language.implicitConversions

final case class GameScene() extends EmptyScene {
  type SceneModel     = GameModel
  type SceneViewModel = Unit

  def name: SceneName = SceneName("game")

  def modelLens: Lens[Model, SceneModel]             = Lens(m => m.game, (m, sm) => m.copy(game = sm))
  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(_ => (), (vm, _) => vm)
}
