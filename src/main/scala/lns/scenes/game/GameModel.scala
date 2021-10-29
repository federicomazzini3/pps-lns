package lns.scenes.game

import indigo.*
import indigoextras.geometry.Vertex
import lns.StartupData
import lns.core.PrologClient
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomModel
import lns.scenes.game.dungeon.{ DungeonModel, Generator, RoomType, * }
import lns.scenes.game.dungeon.RoomType.*
import lns.scenes.game.room.*
import lns.scenes.game.shot.*

sealed trait GameModel

object GameModel {

  case class GameNotStarted(val prologClient: PrologClient) extends GameModel

  case class GameStarted(
      val dungeon: DungeonModel,
      val room: RoomModel,
      val character: CharacterModel,
      val shots: List[ShotModel] = Nil
  ) extends GameModel

  def initial: GameModel = GameNotStarted(PrologClient())

  def start(startupData: StartupData, rooms: Map[Position, RoomType]): GameModel =
    /** Generation here */
    val dungeonModel: DungeonModel =
      Generator(startupData)(
        BasicGrid(
          rooms //Map((0, 0) -> Arena, (1, 0) -> Empty, (1, 1) -> Arena, (2, 1) -> Item, (3, 1) -> Boss, (1, 2) -> Arena)
        )
      )

    GameStarted(
      dungeonModel,
      dungeonModel.initialRoom,
      CharacterModel.initial(startupData)
    )
}
