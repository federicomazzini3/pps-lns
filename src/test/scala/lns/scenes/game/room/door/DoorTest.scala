package lns.scenes.game.room.door

import org.scalatest.freespec.AnyFreeSpec
import DoorImplicit.*
import DoorImplicit.given
import DoorState.*
import Location.*
import scala.language.implicitConversions

class DoorTest extends AnyFreeSpec {
  "A collection of doors" - {
    "when empty" - {
      val doors: Map[Location, DoorState] = Map()
      "should have size 0" in {
        assert(doors.size == 0)
      }
      "should allow to add a door" in {
        assert(Door.updateWith(Map(), (Left -> Close)).size == 1)
      }
    }
    "when non empty" - {
      val doors2: Map[Location, DoorState] = Door(Left -> Close)
      "should allow to add a door" in {
        assert(Door.updateWith(doors2, (Right -> Close)).size == 2)
      }
      "should allow to add the right door" in {
        assert(Door.updateWith(doors2, (Right -> Close)) == Door.updateWith(Door(Left -> Close), Right -> Close))
      }
      "should allow to close all door" in {
        val doors: Map[Location, DoorState] = (Left -> Open) :+ (Right -> Open) :+ (Above -> Lock)
        assert(
          doors.close
            .filter((_, state) =>
              state match {
                case Close => true
                case _     => false
              }
            )
            .size == 3
        )
      }
      "should allow to open all door" in {
        val doors: Map[Location, DoorState] = (Left -> Open) :+ (Right -> Open) :+ (Above -> Lock)
        assert(
          doors.open
            .filter((_, state) =>
              state match {
                case Open => true
                case _    => false
              }
            )
            .size == 3
        )
      }
      "should allow to lock all door" in {
        val doors: Map[Location, DoorState] = (Left -> Open) :+ (Right -> Open) :+ (Above -> Lock)
        assert(
          doors.lock
            .filter((_, state) =>
              state match {
                case Lock => true
                case _    => false
              }
            )
            .size == 3
        )
      }
    }
    "when a door is added" - {
      "should replace the already defined door" in {
        assert(
          (Left            -> Close) :+ (Left -> Open) ==
            Map() :+ (Left -> Open)
        )
      }
    }
  }
}
