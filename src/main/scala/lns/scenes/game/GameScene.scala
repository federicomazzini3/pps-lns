package lns.scenes.game

import indigo.*
import indigo.scenes.*
import indigo.shared.datatypes.Vector2
import indigo.shared.scenegraph.Group
import lns.StartupData
import lns.core.{ Assets, EmptyScene, Model, ViewModel }
import lns.scenes.game.GameModel.{ GameNotStarted, GameStarted }
import lns.scenes.game.anything.FireModel
import lns.scenes.game.character.*
import lns.scenes.game.dungeon.{ DungeonLoadingView, Generator, Position, RoomType }
import lns.scenes.game.room.{ Boundary, Passage, RoomView }
import lns.scenes.game.room.RoomView.*
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

  def modelLens: Lens[Model, SceneModel] =
    Lens(m => m.game, (m, sm) => m.copy(game = sm))
  def viewModelLens: Lens[ViewModel, SceneViewModel] =
    Lens(vm => vm.game, (vm, svm) => vm.copy(game = svm))

  override def updateModel(
      context: FrameContext[StartupData],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {
    case ShotEvent(p, d) =>
      model match {
        case model @ GameStarted(_, room, _) =>
          Outcome(model.copy(room = room.addShot(ShotModel(p, d))))
        case _ => Outcome(model)
      }

    case FrameTick =>
      model match {

        case model @ GameStarted(dungeon, room, character) =>
          for {
            character <- character.update(context)(room)(character)
            room      <- room.update(context)(character)
            (newRoom, newCharacter) = Passage.verifyPassage(dungeon, room, character)
          } yield model.copy(character = newCharacter, room = newRoom)

        case model @ GameNotStarted(prologClient) if !prologClient.consultDone =>
          prologClient
            .consult(
              context.startUpData.dungeonGenerator.get,
              "generateDungeon(30,L)."
            )
            .map(pi => model.copy(prologClient = pi))
        case _ => model
      }

    case PrologEvent.Answer(queryId, substitution) =>
      model match {
        case model @ GameNotStarted(prologClient) if prologClient.hasQuery(queryId) =>
          GameModel.start(
            context.startUpData,
            Generator.getDungeon(substitution)
          )
        case _ => model
      }

    case _ =>
      model
  }

  override def updateViewModel(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): GlobalEvent => Outcome[SceneViewModel] = {

    case FrameTick =>
      model match {
        case model @ GameStarted(_, _, character) =>
          for {
            updatedCharacter <- viewModel.character.update(context, character)
          } yield viewModel.copy(character = updatedCharacter)

        case _ => Outcome(viewModel)
      }

    case _ =>
      Outcome(viewModel)
  }

  override def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    model match {

      case GameStarted(dungeon, room, character) =>
        SceneUpdateFragment(
          (RoomView.draw(context, room, ()) |+|
            CharacterView().draw(context, character, viewModel.character))
            .fitToScreen(context)(Assets.Rooms.roomSize)
        )

      case _ => DungeonLoadingView(context.startUpData)
    }
}

object GameScene {
  val name: SceneName = SceneName("game")
}

extension (group: Group) {
  def |+|(child: Group): Group = group.addChild(child)
  def fitToScreen(context: FrameContext[StartupData])(edge: Int): Group =
    group
      .withScale(Vector2(context.startUpData.scale(edge)))
      .withRef(edge / 2, edge / 2)
      .moveTo(context.startUpData.screenDimensions.center)
}
