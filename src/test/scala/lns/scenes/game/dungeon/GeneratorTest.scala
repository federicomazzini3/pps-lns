package lns.scenes.game.dungeon

import indigo.shared.datatypes.Rectangle
import lns.StartupData

object GeneratorTest extends App {
  import RoomType.*
  val plan: Map[Position, RoomType] = Map(14 -> Arena, 15 -> Empty, 21 -> Arena, 22 -> Item, 23 -> Boss, 27 -> Arena)

  var startupData = StartupData(screenDimensions = Rectangle(0, 0, 0, 0))
  val dungeon     = Generator(startupData)(BasicGrid(5, 6, plan))

  println(plan)
  println(dungeon)
}
