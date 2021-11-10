package lns.scenes.game.character

import scala.language.implicitConversions

import indigo.shared.Outcome
import indigo.shared.constants.Key
import indigo.shared.events.{ InputState, KeyboardEvent }
import indigo.shared.input.*
import indigo.shared.events.KeyboardEvent.KeyDown
import indigoextras.geometry.{ BoundingBox, Vertex }
import indigo.shared.datatypes.Vector2
import lns.StartupData
import lns.core.ContextFixture
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.DynamicState
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomModel
import lns.scenes.game.shot.ShotEvent
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }

trait CharacterModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  def inputKeys(keys: List[KeyboardEvent]): InputState = new InputState(
    Mouse.default,
    Keyboard.calculateNext(Keyboard.default, keys),
    Gamepad.default
  )

  var model: CharacterModel = _
  // var noInvincibilityModel: CharacterModel = _

  val startLife     = 100
  val hitDamage1    = 50
  val hitDamage2    = 150
  val invincibility = 2

  override def beforeEach() = {
    model = new CharacterModel(BoundingBox(roomCenterX, roomCenterY, 10, 10), 0, Stats.Isaac)

    super.beforeEach()
  }

  def checkNewBoundingBox(updatedModel: CharacterModel, x: Int, y: Int): Unit =
    assert(
      updatedModel.boundingBox.x == roomCenterX + x && updatedModel.boundingBox.y == roomCenterY + y
    )
}

class CharacterModelTest extends AnyFreeSpec with CharacterModelFixture {

  "A CharacterModel placed in room center" - {

    "if no direction keys are pressed" - {
      "after one frame update with time delta = 1s" - {
        "should not move" in {
          val updatedModel = model
            .update(getContext(1))(room)(model)
            .getOrElse(fail("Undefined Model"))

          assert(
            updatedModel.boundingBox.x == roomCenterX && updatedModel.boundingBox.y == roomCenterY
          )
          assert(updatedModel.isMoving() == false)
        }
      }
    }

    "if the direction keys are pressed " - {
      List(1, 2).map(second =>
        s"after one frame update with time delta = ${second}s" - {
          Map(
            "Left + Up"    -> List(KeyDown(Key.KEY_A), KeyDown(Key.KEY_W)),
            "Left + Down"  -> List(KeyDown(Key.KEY_A), KeyDown(Key.KEY_S)),
            "Left"         -> List(KeyDown(Key.KEY_A)),
            "Right + Up"   -> List(KeyDown(Key.KEY_D), KeyDown(Key.KEY_W)),
            "Right + Down" -> List(KeyDown(Key.KEY_D), KeyDown(Key.KEY_S)),
            "Right"        -> List(KeyDown(Key.KEY_D)),
            "Up"           -> List(KeyDown(Key.KEY_W)),
            "Down"         -> List(KeyDown(Key.KEY_S))
          ).foreach { keys =>
            s"with keys '${keys._1}' should move correctly" in {
              val updatedModel = model
                .update(getContext(second, inputKeys(keys._2)))(room)(model)
                .getOrElse(fail("Undefined Model"))

              val maxSpeed = MaxSpeed @@ model.stats

              keys._1 match {
                case "Left + Up" =>
                  checkNewBoundingBox(updatedModel, -maxSpeed * second, -maxSpeed * second)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_LEFT)
                case "Left + Down" =>
                  checkNewBoundingBox(updatedModel, -maxSpeed * second, maxSpeed * second)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_LEFT)
                case "Left" =>
                  checkNewBoundingBox(updatedModel, -maxSpeed * second, 0)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_LEFT)
                case "Right + Up" =>
                  checkNewBoundingBox(updatedModel, maxSpeed * second, -maxSpeed * second)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_RIGHT)
                case "Right + Down" =>
                  checkNewBoundingBox(updatedModel, maxSpeed * second, maxSpeed * second)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_RIGHT)
                case "Right" =>
                  checkNewBoundingBox(updatedModel, maxSpeed * second, 0)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_RIGHT)
                case "Up" =>
                  checkNewBoundingBox(updatedModel, 0, -maxSpeed * second)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_UP)
                case "Down" =>
                  checkNewBoundingBox(updatedModel, 0, maxSpeed * second)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_DOWN)
              }

              assert(updatedModel.isMoving() == true)
            }
          }
        }
      )
    }

    "if the shot keys are pressed " - {
      s"after one frame update with time delta 1s" - {
        Map(
          "Up"    -> (List(KeyDown(Key.UP_ARROW)), Vector2(0, -1)),
          "Right" -> (List(KeyDown(Key.RIGHT_ARROW)), Vector2(1, 0)),
          "Down"  -> (List(KeyDown(Key.DOWN_ARROW)), Vector2(0, 1)),
          "Left"  -> (List(KeyDown(Key.LEFT_ARROW)), Vector2(-1, 0))
        ).foreach { keys =>
          s"with keys '${keys._1}' should fire correctly generating ShotEvent" in {
            val updatedModelOutcome = model
              .update(getContext(1, inputKeys(keys._2._1)))(room)(model)

            val updatedModel = updatedModelOutcome.getOrElse(fail("Undefined Model"))

            val updatedPosition =
              Vector2(updatedModel.boundingBox.horizontalCenter, updatedModel.boundingBox.top + updatedModel.shotOffset)

            assert(updatedModelOutcome.globalEventsOrNil == List(ShotEvent(updatedPosition, keys._2._2)))
          }
        }
      }
    }
  }

  "A CharacterModel should have stats" - {
    "start with initial Isaac stats" - {
      Stats.Isaac.foreach { case (key, value) =>
        s"$key as $value" in {
          assert(model.stats(key) == Stats.Isaac(key))
        }
      }
    }
    "should change his stats during gameplay" - {
      "all" in {
        val newStats = Stats(
          MaxLife       -> 20,
          Invincibility -> 3,
          MaxSpeed      -> 600,
          Damage        -> 1,
          FireDamage    -> 6.0,
          FireRange     -> 1000,
          FireRate      -> 0.8
        )
        val updatedModel = model.changeStats(getContext(1), newStats).getOrElse(fail("Undefined Model"))

        assert(updatedModel.stats == newStats)
      }
      "replace one" - {
        val newValue = 100
        Stats.Isaac.foreach { case (key, value) =>
          s"$key from $value to $newValue" in {
            val updatedModel = model.changeStat(getContext(1), (key, newValue)).getOrElse(fail("Undefined Model"))
            assert(key @@ updatedModel.stats == newValue)
          }
        }
      }
      "sum one" - {
        val newValue = 100
        Stats.Isaac.foreach { case (key, value) =>
          s"$key from $value add $newValue = ${Stats.Isaac(key) + newValue}" in {
            val updatedModel = model.sumStat(getContext(1), (key, newValue)).getOrElse(fail("Undefined Model"))

            assert(key @@ updatedModel.stats == Stats.Isaac(key) + newValue)
          }
        }
      }
    }
  }
}
