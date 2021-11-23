package lns.scenes.game.dungeon

import lns.scenes.game.room.door.*
import Location.*
import lns.scenes.game.room.{ Cell, Floor }

object GeneratorHelper {

  val minCell = 0

  val maxCell = Floor.n - 1

  val middle = maxCell / 2

  val doorsMapping = Map(
    Left  -> s"($minCell,$middle)",
    Right -> s"($maxCell,$middle)",
    Above -> s"($middle,$minCell)",
    Below -> s"($middle,$maxCell)"
  )

  def fromLocations(doors: Doors) =
    doors.foldLeft("[")((list, door) => list + doorsMapping(door._1) + ",").dropRight(1) + "]"

  def rule(doors: Doors) =
    val D = fromLocations(doors)
    s"place_stones($maxCell, $D, G, A, S)."

  val cellPattern = "\\(([0-9]+), ([0-9]+)\\)".r

  def fromPrologList(string: String): Seq[Cell] = cellPattern
    .findAllIn(string)
    .map { s =>
      val cellPattern(x, y) = s
      Cell(x.toInt, y.toInt)
    }
    .toSeq
}
