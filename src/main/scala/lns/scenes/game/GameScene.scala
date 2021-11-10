package lns.scenes.game

import indigo.*
import indigo.scenes.*
import indigo.shared.datatypes.Vector2
import indigo.shared.scenegraph.Group
import lns.StartupData
import lns.core.{ Assets, EmptyScene, Model, ViewModel }
import lns.scenes.game.GameModel
import lns.scenes.game.GameViewModel
import lns.scenes.game.HUD.HUDView
import lns.scenes.game.anything.FireModel
import lns.scenes.game.character.*
import lns.scenes.game.dungeon.{ DungeonLoadingView, Generator, Position, RoomType }
import lns.scenes.game.room.{ Boundary, Passage, RoomView }
import lns.scenes.game.room.RoomView.*
import lns.scenes.game.character.*
import lns.scenes.game.room.{ ArenaRoom, RoomModel, RoomView }
import lns.scenes.game.shot.*
import lns.subsystems.prolog.PrologEvent

import scala.collection.immutable.HashMap
import scala.language.implicitConversions

case class GameContext(room: RoomModel, character: CharacterModel)

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
        case model @ GameModel.Started(_, room, _) =>
          Outcome(model.copy(room = room.addShot(ShotModel(p, d))))
        case _ => Outcome(model)
      }

    case FrameTick =>
      model match {

        case model @ GameModel.Started(dungeon, room, character) =>
          val gameContext = GameContext(room, character)
          for {
            character <- character.update(context)(gameContext)
            room      <- room.update(context)(character)
            (newRoom, newCharacter) = Passage.verifyPassage(dungeon, room, character)
          } yield model.copy(character = newCharacter, room = newRoom)

        case model @ GameModel.NotStarted(prologClient) if !prologClient.consultDone =>
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
        case GameModel.NotStarted(prologClient) if prologClient.hasQuery(queryId) =>
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
      (model, viewModel) match {
        case (model: GameModel.Started, viewModel @ GameViewModel.Started(dungeon, room, character)) =>
          for {
            updatedCharacter <- viewModel.character.update(context, model.character)
            newRoom =
              if (room.positionInDungeon != model.room.positionInDungeon)
                viewModel.dungeon.content(model.room.positionInDungeon)
              else room
            updatedRoom <- newRoom.update(context, model.room)
          } yield viewModel.copy(character = updatedCharacter, room = updatedRoom)

        case (model: GameModel.Started, _) => Outcome(GameViewModel.start(model))
        case _                             => Outcome(viewModel)

      }

    case _ =>
      Outcome(viewModel)
  }

  override def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    (model, viewModel) match {

      case (GameModel.Started(dungeon, room, character), viewModel: GameViewModel.Started) =>
        SceneUpdateFragment.empty
          .addLayers(
            Layer(
              BindingKey("game"),
              (RoomView.draw(context, room, viewModel.room) |+|
                CharacterView().draw(context, character, viewModel.character))
                .fitToScreen(context)(Assets.Rooms.roomSize)
            ),
            Layer(BindingKey("HUD"), (HUDView.draw(context, character)))
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
