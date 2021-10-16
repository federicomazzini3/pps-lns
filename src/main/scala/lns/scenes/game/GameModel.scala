package lns.scenes.game

import lns.StartupData
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomModel

case class GameModel(val character: CharacterModel, val room: RoomModel)

object GameModel {
  def initial(startupData: StartupData): GameModel =
    GameModel(
      CharacterModel.initial(startupData),
      RoomModel.initial(startupData)
    )
}
