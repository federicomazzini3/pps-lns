package lns.scenes.game

import indigo.*
import indigo.scenes.*
import lns.StartupData
import lns.core.{ Assets, EmptyScene, Model, ViewModel }
import lns.scenes.game.GameModel.{ GameNotStarted, GameStarted }
import lns.scenes.game.character.*
import lns.scenes.game.dungeon.{ DungeonLoadingView, Generator, Position, RoomType }
import lns.scenes.game.room.{ Boundary, Passage, RoomView }
import lns.scenes.game.room.RoomView.*
import lns.scenes.game.room.CharacterExtension.boundMovement
import lns.scenes.game.character.*
import lns.scenes.game.room.{ ArenaRoom, RoomModel, RoomView }
import lns.scenes.game.room.RoomView.*
import lns.scenes.game.shot.*
import lns.subsystems.prolog.PrologEvent

import scala.collection.immutable.HashMap
import scala.language.implicitConversions

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
      model match {
        case model @ GameStarted(_, _, _, shots) =>
          val updatedShots: List[ShotModel] = shots :+ ShotModel(p, d)
          Outcome(model.copy(shots = updatedShots))
      }

    case FrameTick =>
      model match {

        case model @ GameStarted(dungeon, room, character, shots) =>
          for {
            character <- character.update(context)(room)
            (newRoom, newCharacter) = Passage.verifyPassage(dungeon, room, character)
            updatedShots = for {
              shot <- shots
              updatedShot = shot.update(context)(room).unsafeGet
            } yield updatedShot
          } yield model.copy(character = newCharacter, room = newRoom, shots = updatedShots)

        case model @ GameNotStarted(prologClient) if !prologClient.consultDone =>
          prologClient
            .consult(context.startUpData.dungeonGenerator.get, "generateDungeon(30,L).")
            .map(pi => model.copy(prologClient = pi))
        case _ => model
      }

    case PrologEvent.Answer(queryId, substitution) =>
      model match {
        case model @ GameNotStarted(prologClient) if prologClient.hasQuery(queryId) =>
          GameModel.start(context.startUpData, Generator.getDungeon(substitution))
        case _ => model
      }

    case _ =>
      model
  }

  override def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    model match {

      case GameStarted(dungeon, room, character, shots) =>
        var scene = RoomView.draw(context, room, ()) |+|
          CharacterView().draw(context, character, ())

        shots.map { s =>
          scene = scene |+| ShotView().draw(context, s, ())
        }
        scene

      case _ => DungeonLoadingView(context.startUpData)
    }
}

object GameScene {
  val name: SceneName = SceneName("game")
}
