package lns.scenes.game

import lns.StartupData
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomModel
import lns.scenes.game.dungeon.{ DungeonModel, Generator }
import lns.scenes.game.dungeon.*
import lns.scenes.game.dungeon.RoomType.*

sealed trait GameModel

object GameModel {

  case object GameNotStarted extends GameModel

  case class GameStarted(val dungeon: DungeonModel, val room: RoomModel, val character: CharacterModel)
      extends GameModel

  def initial: GameModel = GameNotStarted

  def start(startupData: StartupData): GameModel =
    /** Generation here */
    val dungeonModel: DungeonModel =
      Generator(startupData)(
        BasicGrid(
          6,
          6,
          Map((0, 0) -> Arena, (1, 0) -> Empty, (1, 1) -> Arena, (2, 1) -> Item, (3, 1) -> Boss, (1, 2) -> Arena)
        )
      )

    GameStarted(
      dungeonModel,
      dungeonModel.initialRoom,
      CharacterModel.initial(startupData)
    )
}
