package lns.scenes.game

import lns.StartupData
import lns.scenes.game.character.*

case class GameModel(val character: Character) {}

object GameModel {
  def initial(startupData: StartupData): GameModel = GameModel(Character.initial(startupData))

}
