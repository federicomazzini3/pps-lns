package lns.scenes.game

import indigo.*
import indigo.scenes.*
import indigo.shared.datatypes.Vector2
import indigo.shared.scenegraph.Group
import lns.StartupData
import lns.core.{ Assets, EmptyScene, Model, ViewModel }
import lns.scenes.end.Restart
import lns.scenes.game.GameModel
import lns.scenes.game.GameViewModel
import lns.scenes.game.hud.HUDView
import lns.scenes.game.map.MapView
import lns.scenes.game.anything.FireModel
import lns.scenes.game.characters.*
import lns.scenes.game.dungeon.*
import lns.scenes.game.dungeon.{ DungeonLoadingView, Generator, Position, RoomType }
import lns.scenes.game.dungeon.GeneratorHelper as GenHelper
import lns.scenes.game.room.{ ArenaRoom, BossRoom, RoomModel, RoomView, * }
import lns.scenes.game.room.RoomView.*
import lns.scenes.game.characters.*
import lns.scenes.game.enemies.PrologEnemyModel
import lns.scenes.game.shots.*
import lns.subsystems.prolog.PrologEvent
import lns.scenes.game.subsystems.{ BattleEventSubSystems, Dead, Hit, ResetSubsystem }
import lns.scenes.game.collisions.CollisionUpdater.*

import scala.collection.immutable.HashMap
import scala.language.implicitConversions

case class GameContext(room: RoomModel, character: CharacterModel)

final case class GameScene(screenDimensions: Rectangle) extends EmptyScene {
  type SceneModel     = GameModel
  type SceneViewModel = GameViewModel

  def name: SceneName = GameScene.name

  override def eventFilters: EventFilters = EventFilters.Permissive

  override val subSystems: Set[SubSystem] =
    Set(
      BattleEventSubSystems(screenDimensions)
    )

  def modelLens: Lens[Model, SceneModel] =
    Lens(m => m.game, (m, sm) => m.copy(game = sm))
  def viewModelLens: Lens[ViewModel, SceneViewModel] =
    Lens(vm => vm.game, (vm, svm) => vm.copy(game = svm))

  override def updateModel(
      context: FrameContext[StartupData],
      model: SceneModel
  ): GlobalEvent => Outcome[SceneModel] = {
    case FrameTick =>
      model match {

        case model @ GameModel.Started(_, dungeon, roomIndex, character) if dungeon.generated =>
          val gameContext = GameContext(model.currentRoom, character)
          for {
            updatedCharacter <- model.updateCharacter(c => character.update(context)(gameContext))
            updatedRoom      <- updatedCharacter.updateCurrentRoom(r => model.currentRoom.update(context)(character))
            withPassage      <- updatedRoom.updateWithPassage
            withStats        <- withPassage.updateStatsAfterCollision(context)
            withMovements    <- withStats.updateMovementAfterCollision
          } yield withMovements

        case model: GameModel.Started if !model.dungeon.generated && !model.prologClient.consultDone =>
          model.dungeon.firstRoomToGenerate match {
            case Some(room) =>
              println(GeneratorHelper.rule(room.doors))
              model.prologClient
                .consult(
                  context.startUpData.getPrologFile("blocking_elements_generator").get,
                  GeneratorHelper.rule(room.doors)
                )
                .map(pi => model.copy(prologClient = pi))
            case _ => model
          }

        case model @ GameModel.NotStarted(prologClient) if !prologClient.consultDone =>
          prologClient
            .consult(
              context.startUpData.getPrologFile("dungeon_generator").get,
              "generateDungeon(30,L)."
            )
            .map(pi => model.copy(prologClient = pi))

        case _ => model
      }

    case ShotEvent(shot) =>
      model match {
        case model @ GameModel.Started(_, _, room, _) =>
          model.updateCurrentRoom(room => model.currentRoom.addShot(shot))
        case _ => Outcome(model)
      }

    case PrologEvent.Answer(queryId, substitution) =>
      model match {
        case GameModel.NotStarted(prologClient) if prologClient.hasQuery(queryId) =>
          GameModel.start(
            context.startUpData,
            Generator.getDungeon(substitution)
          )
        case model: GameModel.Started if model.prologClient.hasQuery(queryId) =>
          model.dungeon.firstRoomToGenerate match {
            case Some(room) =>
              model.updateRoom(room.positionInDungeon)(room =>
                room.addAnythings(Generator.generateElementsFromProlog(substitution, room))
              )
            case _ => model
          }
        case model @ GameModel.Started(_, dungeon, roomIndex, character) =>
          model.updateEachAnythings { anything =>
            anything match {
              case anything: PrologEnemyModel if anything.prologClient.hasQuery(queryId) =>
                anything.behaviour(substitution)
              case _ => Outcome(anything)
            }
          }
        case _ => model
      }

    case Restart => Outcome(GameModel.initial).addGlobalEvents(ResetSubsystem)

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
            updatedCharacter <- viewModel.character.update(context)(model.character)
            newRoom =
              if (room.positionInDungeon != model.currentRoomPosition)
                viewModel.dungeon.content(model.currentRoomPosition)
              else room
            updatedRoom <- newRoom.update(context, model.currentRoom)
          } yield viewModel.copy(character = updatedCharacter, room = updatedRoom)

        case (model: GameModel.Started, _) if model.dungeon.generated => Outcome(GameViewModel.start(model))
        case _                                                        => Outcome(viewModel)

      }

    case Restart => Outcome(GameViewModel.initial(context.startUpData, GameModel.initial))

    case _ =>
      Outcome(viewModel)
  }

  override def present(
      context: FrameContext[StartupData],
      model: SceneModel,
      viewModel: SceneViewModel
  ): Outcome[SceneUpdateFragment] =
    (model, viewModel) match {

      case (model @ GameModel.Started(_, dungeon, room, character), viewModel: GameViewModel.Started)
          if dungeon.generated =>
        SceneUpdateFragment.empty
          .addLayers(
            Layer(
              BindingKey("game"),
              (RoomView.draw(context, model.currentRoom, viewModel.room) |+|
                CharacterView.draw(context, character, viewModel.character))
                .fitToScreen(context)(Assets.Rooms.roomSize)
            ),
            Layer(
              BindingKey("HUD"),
              List(HUDView.draw(context, character), MapView.draw(context, dungeon, room))
            )
          )
      case (model @ GameModel.Started(_, dungeon, _, _), _) => DungeonLoadingView(context.startUpData, model.dungeon)
      case _                                                => DungeonLoadingView(context.startUpData)
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
