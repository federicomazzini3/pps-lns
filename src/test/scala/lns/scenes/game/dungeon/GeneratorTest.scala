package lns.scenes.game.dungeon

import indigo.shared.datatypes.Rectangle
import lns.StartupData
import lns.scenes.game.room.EmptyRoom

object GeneratorTest extends App {
  import RoomType.*
  val plan: Map[Position, RoomType] =
    Map((0, 0) -> Arena, (0, 1) -> Empty, (1, 1) -> Arena, (1, 2) -> Item, (1, 3) -> Boss, (2, 1) -> Arena)

  var startupData = StartupData(screenDimensions = Rectangle(0, 0, 0, 0))
  val dungeon     = Generator(startupData)(BasicGrid(plan))

  println(plan)
  println(dungeon)

  println(
    dungeon.content
      .collect { case (pos, EmptyRoom(_, _, _)) => pos }
      .map(pos => dungeon.content(pos))
      .head
  )
}
