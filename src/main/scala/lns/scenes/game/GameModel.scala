package lns.scenes.game

import lns.StartupData
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomModel
import lns.scenes.game.dungeon.{ DungeonModel, Generator }
import lns.scenes.game.dungeon.*
import lns.scenes.game.dungeon.RoomType.*

case class GameModel(val dungeon: DungeonModel, val room: RoomModel, val character: CharacterModel)

object GameModel {
  def initial(startupData: StartupData): GameModel =
    val dungeonModel: DungeonModel =
      Generator(startupData)(
        BasicGrid(6, 6, Map(14 -> Arena, 15 -> Empty, 21 -> Arena, 22 -> Item, 23 -> Boss, 27 -> Arena))
      )
    GameModel(
      dungeonModel,
      dungeonModel.initialRoom,
      CharacterModel.initial(startupData)
    )
}
