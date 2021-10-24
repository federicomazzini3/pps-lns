package lns.scenes.game

import indigo.*
import indigo.scenes.*
import lns.StartupData
import lns.core.{ EmptyScene, Model, ViewModel }
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomView
import lns.scenes.game.room.RoomView.*
import lns.scenes.game.shot.*

import scala.language.implicitConversions

case object MyEvent extends GlobalEvent

final case class GameScene() extends EmptyScene {
  type SceneModel     = GameModel
  type SceneViewModel = GameViewModel

  def name: SceneName = GameScene.name

  override def eventFilters: EventFilters = EventFilters.Permissive

  def modelLens: Lens[Model, SceneModel]             = Lens(m => m.game, (m, sm) => m.copy(game = sm))
  def viewModelLens: Lens[ViewModel, SceneViewModel] = Lens(vm => vm.game, (vm, svm) => vm.copy(game = svm))

  override def updateModel(
      context: FrameContext[StartupData],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {
    case ShotEvent(p, d) =>
      val updatedShots: List[ShotModel] = model.shots :+ ShotModel(p, d)
      Outcome(model.copy(shots = updatedShots))

    case FrameTick =>
      for {
        updatedCharacter <- model.character.update(context)(model.room)
        updatedShots = for {
          shot <- model.shots
          updatedShot = shot.update(context)(model.room).unsafeGet
        } yield updatedShot
        updatedGameModel <- model.copy(character = updatedCharacter, shots = updatedShots)
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
    var scene = RoomView.draw(context, model.room, ()) |+|
      CharacterView().draw(context, model.character, ())

    model.shots.map { s =>
      scene = scene |+| ShotView().draw(context, s, ())
    }
    scene
}

object GameScene {
  val name: SceneName = SceneName("game")
}
