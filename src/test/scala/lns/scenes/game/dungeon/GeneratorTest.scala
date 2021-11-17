package lns.scenes.game.dungeon

import indigo.shared.datatypes.Rectangle
import lns.StartupData
import lns.scenes.game.room.EmptyRoom
import org.scalatest.Suite
import org.scalatest.freespec.AnyFreeSpec
import lns.subsystems.prolog.{ Substitution, Term }
import lns.subsystems.prolog.Term.*
import RoomType.*

class GeneratorTest extends AnyFreeSpec { this: Suite =>

  val grid = Map((0, 0) -> Start, (1, 0) -> Empty, (2, 0) -> Arena, (0, 1) -> Item, (0, 2) -> Boss)

  "a Dungeon Generator should" - {
    "parse Polog result to a Map of Position -> RoomType" in {
      val test: Substitution = Substitution(
        Map(
          "L" ->
            Struct(
              Atom("."),
              Struct(Atom("room"), Num(0, false), Num(0, false), Atom("s")),
              Struct(
                Atom("."),
                Struct(Atom("room"), Num(1, false), Num(0, false), Atom("e")),
                Struct(
                  Atom("."),
                  Struct(Atom("room"), Num(2, false), Num(0, false), Atom("a")),
                  Struct(
                    Atom("."),
                    Struct(Atom("room"), Num(0, false), Num(1, false), Atom("i")),
                    Struct(Atom("."), Struct(Atom("room"), Num(0, false), Num(2, false), Atom("b")), Atom("[]"))
                  )
                )
              )
            )
        )
      )

      assert(Generator.getDungeon(test) == grid)
    }
    /*
    "generate the model from a Map of Position -> RoomType" in {
      val plan: Map[Position, RoomType] = grid

      var startupData = StartupData(screenDimensions = Rectangle(0, 0, 0, 0))
      val dungeon     = Generator(BasicGrid(plan))

      println(plan)
      println(dungeon)
    }
     */
  }
}
