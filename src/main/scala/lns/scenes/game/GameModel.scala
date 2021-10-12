package lns.scenes.game

import lns.StartupData
import lns.scenes.game.character.*

case class GameModel(val character: CharacterModel) {}

object GameModel {
  def initial(startupData: StartupData): GameModel = GameModel(CharacterModel.initial(startupData))

}
