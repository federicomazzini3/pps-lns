package lns.scenes.game.dungeon

import indigo.shared.datatypes.Rectangle
import lns.StartupData
import lns.scenes.game.room.EmptyRoom

object GeneratorTest extends App {
  import RoomType.*
  val plan: Map[Position, RoomType] = Map(14 -> Arena, 15 -> Empty, 21 -> Arena, 22 -> Item, 23 -> Boss, 27 -> Arena)

  var startupData = StartupData(screenDimensions = Rectangle(0, 0, 0, 0))
  val dungeon     = Generator(startupData)(BasicGrid(5, 6, plan))

  println(plan)
  println(dungeon)

  println(
    dungeon.content
      .collect { case (pos, EmptyRoom(_, _, _)) => pos }
      .map(pos => dungeon.content(pos))
      .head
  )
}
