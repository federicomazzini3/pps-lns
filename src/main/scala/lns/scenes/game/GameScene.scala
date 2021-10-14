package lns.scenes.game

import indigo.*
import indigo.scenes.*
import lns.StartupData
import lns.core.{ EmptyScene, Model, ViewModel }
import scala.language.implicitConversions

final case class GameScene() extends EmptyScene {
  type SceneModel     = GameModel
  type SceneViewModel = GameViewModel

  def name: SceneName = GameScene.name

  def modelLens: Lens[Model, SceneModel]             = Lens(m => m.game, (m, sm) => m.copy(game = sm))
  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(vm => vm.game, (vm, svm) => vm.copy(game = svm))

  override def updateModel(
      context: FrameContext[StartupData],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {
    case FrameTick =>
      for {
        updatedCharacter <- model.character.update(context)
        updatedGameModel <- model.copy(character = updatedCharacter)
        // updatedRoom       <- model.room.update(context)
        // updatedGameModel <- model.copy(character = updatedCharacter, room = updatedRoom)
        // updatedGameModel2 <- fun(updatedGameModel.character, updatedGameModel.room.getRobaCheFaMale())
      } yield updatedGameModel

    case _ =>
      Outcome(model)
  }

  override def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    GameView.draw(
      context.startUpData,
      model,
      viewModel
    ) |+| CharacterView().draw(context, model.character, ())
}

object GameScene {
  val name: SceneName = SceneName("game")
}
