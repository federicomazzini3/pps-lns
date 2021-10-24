package lns.scenes.game

import indigo.*
import indigoextras.geometry.Vertex
import lns.StartupData
import lns.scenes.game.character.*
import lns.scenes.game.room.*
import lns.scenes.game.shot.*

case class GameModel(val character: CharacterModel, val room: RoomModel, val shots: List[ShotModel] = Nil)

object GameModel {
  def initial(startupData: StartupData): GameModel =
    GameModel(
      CharacterModel.initial(startupData),
      RoomModel.initial(startupData)
    )
}
