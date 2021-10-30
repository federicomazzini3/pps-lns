package lns.scenes.game.dungeon

import indigo.shared.datatypes.Rectangle
import lns.StartupData
import lns.scenes.game.room.EmptyRoom

// TODO: test getDungeon()
// val test: Term =
//     Compound(Atom("."), Compound(Atom("room"), Num(0, false), Num(0, false), Atom("s")), Compound(Atom(".")))

object GeneratorTest extends App {
  import RoomType.*
  val plan: Map[Position, RoomType] =
    Map((0, 0) -> Arena, (0, 1) -> Empty, (1, 1) -> Arena, (1, 2) -> Item, (1, 3) -> Boss, (2, 1) -> Arena)

  var startupData = StartupData(screenDimensions = Rectangle(0, 0, 0, 0))
  val dungeon     = Generator(BasicGrid(plan))

  println(plan)
  println(dungeon)
}
