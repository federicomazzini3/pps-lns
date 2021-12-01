package lns.scenes.game.room

import lns.core.Assets
import scala.util.Random

/**
 * Series of method to create and manage a floor inside a room
 */
object Floor {
  val n: Int       = (Assets.Rooms.floorSize / Assets.Rooms.cellSize).toInt
  val nCenter: Int = n - 1
  val center: Cell = Cell((n - 1) / 2, (n - 1) / 2)
  val random: Cell = Cell(Random.between(0, n), Random.between(0, n))
}

/**
 * Model for a cell that compose floor
 * @param x
 *   the column number in grid
 * @param y
 *   the row number in grid
 */
case class Cell(x: Int, y: Int)
