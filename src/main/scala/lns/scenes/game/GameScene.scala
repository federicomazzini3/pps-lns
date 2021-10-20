package lns.scenes.game

import indigo.*
import indigo.scenes.*
import lns.StartupData
import lns.core.{ EmptyScene, Model, ViewModel }
import lns.scenes.game.character.*
import lns.scenes.game.room.{ Boundary, Passage, RoomView }
import lns.scenes.game.room.RoomView.*

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
        character <- model.character.update(context)
        boundedCharacterLocation = Boundary.characterBounded(model.room.floor, character.boundingBox)
        characterBounded         = character.copy(boundingBox = character.boundingBox.moveTo(boundedCharacterLocation))
        (newRoom, newCharacter)  = Passage.currentRoom(model.dungeon, model.room, characterBounded)
      } yield model.copy(character = newCharacter, room = newRoom)

    case _ =>
      Outcome(model)
  }

  override def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    RoomView.draw(context, model.room, ()) |+|
      CharacterView().draw(context, model.character, ())
}

object GameScene {
  val name: SceneName = SceneName("game")
}
