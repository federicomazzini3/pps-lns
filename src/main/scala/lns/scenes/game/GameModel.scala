package lns.scenes.game

sealed trait GameModel

object GameModel {
  val initial: GameModel = GameModelImpl()

  private case class GameModelImpl() extends GameModel
}

