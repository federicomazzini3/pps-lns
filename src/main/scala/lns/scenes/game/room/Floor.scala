package lns.scenes.game.room

import lns.core.Assets

import scala.util.Random

object Floor {
  val n: Int       = (Assets.Rooms.floorSize / Assets.Rooms.cellSize).toInt
  val center: Cell = Cell((n - 1) / 2, (n - 1) / 2)
  val random: Cell = Cell(Random.between(0, n), Random.between(0, n))
}

case class Cell(x: Int, y: Int)
