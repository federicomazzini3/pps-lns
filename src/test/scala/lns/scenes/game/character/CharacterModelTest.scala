package lns.scenes.game.character

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
import lns.scenes.game.stats.{ Stats, StatsLens }
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
    model = new CharacterModel(BoundingBox(roomCenterX, roomCenterY, 10, 10), Stats.Isaac)

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

              keys._1 match {
                case "Left + Up" =>
                  checkNewBoundingBox(updatedModel, -model.maxSpeed * second, -model.maxSpeed * second)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_LEFT)
                case "Left + Down" =>
                  checkNewBoundingBox(updatedModel, -model.maxSpeed * second, model.maxSpeed * second)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_LEFT)
                case "Left" =>
                  checkNewBoundingBox(updatedModel, -model.maxSpeed * second, 0)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_LEFT)
                case "Right + Up" =>
                  checkNewBoundingBox(updatedModel, model.maxSpeed * second, -model.maxSpeed * second)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_RIGHT)
                case "Right + Down" =>
                  checkNewBoundingBox(updatedModel, model.maxSpeed * second, model.maxSpeed * second)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_RIGHT)
                case "Right" =>
                  checkNewBoundingBox(updatedModel, model.maxSpeed * second, 0)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_RIGHT)
                case "Up" =>
                  checkNewBoundingBox(updatedModel, 0, -model.maxSpeed * second)
                  assert(updatedModel.getDynamicState() == DynamicState.MOVE_UP)
                case "Down" =>
                  checkNewBoundingBox(updatedModel, 0, model.maxSpeed * second)
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
          "Up"    -> List(KeyDown(Key.UP_ARROW)),
          "Right" -> List(KeyDown(Key.RIGHT_ARROW)),
          "Down"  -> List(KeyDown(Key.DOWN_ARROW)),
          "Left"  -> List(KeyDown(Key.LEFT_ARROW))
        ).foreach { keys =>
          s"with keys '${keys._1}' should fire correctly generating ShotEvent" in {
            val updatedModelOutcome = model
              .update(getContext(1, inputKeys(keys._2)))(room)(model)

            val updatedModel = updatedModelOutcome.getOrElse(fail("Undefined Model"))

            val updatedPosition =
              Vector2(updatedModel.boundingBox.horizontalCenter, updatedModel.boundingBox.verticalCenter)

            keys._1 match {
              case "Up" =>
                assert(updatedModelOutcome.globalEventsOrNil == List(ShotEvent(updatedPosition, Vector2(0, -1))))
              case "Right" =>
                assert(updatedModelOutcome.globalEventsOrNil == List(ShotEvent(updatedPosition, Vector2(1, 0))))
              case "Down" =>
                assert(updatedModelOutcome.globalEventsOrNil == List(ShotEvent(updatedPosition, Vector2(0, 1))))
              case "Left" =>
                assert(updatedModelOutcome.globalEventsOrNil == List(ShotEvent(updatedPosition, Vector2(-1, 0))))
            }
          }
        }
      }
    }
  }

  "A CharacterModel should have stats" - {
    "start with initial Isaac stats" - {
      "maxLife" in {
        assert(model.maxLife == Stats.Isaac.maxLife)
      }
      "invincibility" in {
        assert(model.invincibility == Stats.Isaac.invincibility)
      }
      "maxSpeed" in {
        assert(model.maxSpeed == Stats.Isaac.maxSpeed)
      }
      "contactDamage" in {
        assert(model.damage == Stats.Isaac.damage)
      }
      "fireDamage" in {
        assert(model.fireDamage == Stats.Isaac.fireDamage)
      }
      "fireRange" in {
        assert(model.fireRange == Stats.Isaac.fireRange)
      }
      "fireRate" in {
        assert(model.fireRate == Stats.Isaac.fireRate)
      }
    }
    "should change the stats during gameplay" - {
      "all" in {
        val newStats = Stats(
          maxLife = 20,
          invincibility = 3,
          maxSpeed = 600,
          damage = 1,
          fireDamage = 4,
          fireRange = 600,
          fireRate = 1.2
        )
        val updatedModel = model.changeStats(getContext(1), newStats).getOrElse(fail("Undefined Model"))

        assert(updatedModel.stats == newStats)
      }
      "only maxLife" in {
        val updatedModel = model.changeStat(getContext(1), "maxLife", 100).getOrElse(fail("Undefined Model"))

        val compareStats = StatsLens.maxLife.set(Stats.Isaac, 100)
        assert(updatedModel.stats == compareStats)
      }
      "only invincibility" in {
        val updatedModel = model.changeStat(getContext(1), "invincibility", 5).getOrElse(fail("Undefined Model"))

        val compareStats = StatsLens.invincibility.set(Stats.Isaac, 5)
        assert(updatedModel.stats == compareStats)
      }
      "only maxSpeed" in {
        val updatedModel = model.changeStat(getContext(1), "maxSpeed", 5).getOrElse(fail("Undefined Model"))

        val compareStats = StatsLens.maxSpeed.set(Stats.Isaac, 5)
        assert(updatedModel.stats == compareStats)
      }
      "only damage" in {
        val updatedModel = model.changeStat(getContext(1), "damage", 5).getOrElse(fail("Undefined Model"))

        val compareStats = StatsLens.damage.set(Stats.Isaac, 5)
        assert(updatedModel.stats == compareStats)
      }
      "only fireDamage" in {
        val updatedModel = model.changeStat(getContext(1), "fireDamage", 5).getOrElse(fail("Undefined Model"))

        val compareStats = StatsLens.fireDamage.set(Stats.Isaac, 5)
        assert(updatedModel.stats == compareStats)
      }
      "only fireRange" in {
        val updatedModel = model.changeStat(getContext(1), "fireRange", 5).getOrElse(fail("Undefined Model"))

        val compareStats = StatsLens.fireRange.set(Stats.Isaac, 5)
        assert(updatedModel.stats == compareStats)
      }
      "only fireRate" in {
        val updatedModel = model.changeStat(getContext(1), "fireRate", 5).getOrElse(fail("Undefined Model"))

        val compareStats = StatsLens.fireRate.set(Stats.Isaac, 5)
        assert(updatedModel.stats == compareStats)
      }
    }
  }
}
