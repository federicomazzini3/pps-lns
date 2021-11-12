package lns.scenes.game

import indigo.*
import indigo.shared.FrameContext
import indigoextras.geometry.Vertex
import lns.StartupData
import lns.core.PrologClient
import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.anything.*
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomModel
import lns.scenes.game.dungeon.*
import lns.scenes.game.dungeon.{ DungeonModel, Generator, RoomType, * }
import lns.scenes.game.dungeon.RoomType.*
import lns.scenes.game.room.*
import lns.scenes.game
import lns.scenes.game.shot.*
import lns.scenes.game.updater.*
import lns.scenes.game.updater.CollisionUpdater.*
import lns.scenes.game.updater.PassageUpdater.*

sealed trait GameModel

object GameModel {

  case class NotStarted(val prologClient: PrologClient) extends GameModel

  case class Started(
      val dungeon: DungeonModel,
      val currentRoomPosition: (Int, Int),
      val character: CharacterModel
  ) extends GameModel {

    val allAnythings: Map[AnythingId, AnythingModel] =
      Map(character.id -> character) ++ currentRoom.anythings

    def changeCurrentRoom(newCurrentRoom: (Int, Int)): Started =
      this.copy(currentRoomPosition = newCurrentRoom)

    def currentRoom: RoomModel = dungeon.room(currentRoomPosition).get

    def updateCurrentRoom(f: RoomModel => RoomModel): Started =
      this.copy(dungeon = dungeon.updateRoom(currentRoomPosition)(f(currentRoom)))

    def updateCharacter(f: CharacterModel => CharacterModel): Started =
      this.copy(character = f(character))

    def updateEachAnythingsCurrentRoom(f: AnythingModel => AnythingModel): GameModel.Started =
      updateCurrentRoom(room => room.updateEachAnything(f))

    def updateAnythingsCurrentRoom(
        f: Map[AnythingId, AnythingModel] => Map[AnythingId, AnythingModel]
    ): GameModel.Started =
      updateCurrentRoom(room => room.updateAnythings(f))

    def updateWithPassage: Started =
      val (newRoom, movedCharacter) = PassageUpdater.apply(dungeon, currentRoom, character)
      this
        .changeCurrentRoom(newRoom)
        .updateCharacter(character => movedCharacter)

    def updateStatsAfterCollision(context: FrameContext[StartupData]): GameModel.Started =
      this
        .updateCharacter(character =>
          CollisionUpdater(character)(this.currentRoom.anythings)(updateLife(context))
            .asInstanceOf[CharacterModel]
        )
        .updateEachAnythingsCurrentRoom(anything => CollisionUpdater(anything)(this.allAnythings)(updateLife(context)))
        .updateAnythingsCurrentRoom(anythings =>
          anythings.filter {
            case (id, elem: AliveModel) if elem.life <= 0 => false
            case _                                        => true
          }
        )

    def updateMovementAfterCollision: GameModel.Started =
      this
        .updateCharacter(character =>
          CollisionUpdater(character)(this.currentRoom.anythings)(updateMove)
            .asInstanceOf[CharacterModel]
        )
        .updateEachAnythingsCurrentRoom(anything => CollisionUpdater(anything)(this.allAnythings)(updateMove))
  }

  def initial: GameModel = NotStarted(PrologClient())

  def start(startupData: StartupData, rooms: Map[Position, RoomType]): GameModel =
    /** Generation here */
    val dungeonModel: DungeonModel =
      Generator(
        BasicGrid(
          rooms
        )
      )

    Started(
      dungeonModel,
      dungeonModel.initialRoom,
      CharacterModel.initial
    )
}
