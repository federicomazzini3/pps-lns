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
    val dungeonModel: DungeonModel =
      Generator(startupData)(
        BasicGrid(
          6,
          6,
          Map((0, 0) -> Arena, (0, 1) -> Empty, (1, 1) -> Arena, (1, 2) -> Item, (1, 3) -> Boss, (2, 1) -> Arena)
        )
      )
    GameStarted(
      dungeonModel,
      dungeonModel.initialRoom,
      CharacterModel.initial(startupData)
    )
}
