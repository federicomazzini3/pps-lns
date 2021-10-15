package lns.scenes.game

import lns.StartupData
import lns.scenes.game.character.*
import lns.scenes.game.room.{ Room, EmptyRoom }

case class GameModel(val character: CharacterModel, val room: EmptyRoom)

object GameModel {
  def initial(startupData: StartupData): GameModel =
    GameModel(CharacterModel.initial(startupData), Room.initial(startupData))

}
