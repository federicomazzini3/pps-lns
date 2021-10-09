package lns.scenes.game

import lns.scenes.game.character._

case class GameModel(val character: Character) {}

object GameModel {
  val initial: GameModel = GameModel(Character.initial)

}
