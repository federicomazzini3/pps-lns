package lns.scenes.game

import indigo.*
import indigo.scenes.*
import lns.StartupData
import lns.core.{ Assets, EmptyScene, Model, ViewModel }
import lns.scenes.game.GameModel.{ GameNotStarted, GameStarted }
import lns.scenes.game.character.*
import lns.scenes.game.dungeon.DungeonLoadingView
import lns.scenes.game.room.{ Boundary, Passage, RoomView }
import lns.scenes.game.room.RoomView.*
import lns.scenes.game.room.CharacterExtension.boundMovement

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
      model match {

        case model @ GameStarted(dungeon, room, character) =>
          for {
            character <- character.update(context)
            characterBounded        = character.boundMovement(room.floor)
            (newRoom, newCharacter) = Passage.verifyPassage(dungeon, room, characterBounded)
          } yield model.copy(character = newCharacter, room = newRoom)

        case GameNotStarted => GameModel.start(context.startUpData)
      }

    case _ =>
      Outcome(model)
  }

  override def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    model match {
      case GameStarted(dungeon, room, character) =>
        RoomView.draw(context, room, ()) |+|
          CharacterView().draw(context, character, ())
      case _ => DungeonLoadingView(context.startUpData)
    }

}

object GameScene {
  val name: SceneName = SceneName("game")
}
