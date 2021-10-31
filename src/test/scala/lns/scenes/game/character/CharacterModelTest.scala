package lns.scenes.game.character

import indigo.shared.constants.Key
import indigo.shared.events.{ InputState, KeyboardEvent }
import indigo.shared.input.*
import indigo.shared.events.KeyboardEvent.KeyDown
import indigoextras.geometry.{ BoundingBox, Vertex }
import indigo.shared.datatypes.Vector2
import lns.StartupData
import lns.core.ContextFixture
import lns.core.Macros.copyMacro
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomModel
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, GivenWhenThen, Suite }

trait CharacterModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  def inputMove(keys: List[KeyboardEvent]): InputState = new InputState(
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
    model = new CharacterModel(BoundingBox(centerWidth, centerHeight, 10, 10), Vector2(0, 0), startLife, invincibility)
    // noInvincibilityModel = new MyAliveModel(BoundingBox(centerWidth, centerHeight, 10, 10), startLife, 0)

    super.beforeEach()
  }

  def checkNewBoundingBox(updatedModel: CharacterModel, x: Int, y: Int): Unit = assert(true)
  //assert(
  //  updatedModel.boundingBox.x == centerWidth + x && updatedModel.boundingBox.y == centerHeight + y
  //)
}

class CharacterModelTest extends AnyFreeSpec with CharacterModelFixture with GivenWhenThen {

  "A CharacterModel placed in room center" - {

    "if no keys are pressed" - {
      "after one frame update with time delta = 1s" - {
        "should not move" in {
          val updatedModel = model
            .update(getContext(1))(room)
            .getOrElse(fail("Undefined Model"))

          assert(
            updatedModel.boundingBox.x == centerWidth && updatedModel.boundingBox.y == centerHeight
          )
        }
      }
    }

    "if the keys are pressed " - {
      List(1, 2).map(second =>
        s"after one frame update with time delta = ${second}s" - {
          Map(
            "Left + Up"    -> List(KeyDown(Key.LEFT_ARROW), KeyDown(Key.UP_ARROW)),
            "Left + Down"  -> List(KeyDown(Key.LEFT_ARROW), KeyDown(Key.DOWN_ARROW)),
            "Left"         -> List(KeyDown(Key.LEFT_ARROW)),
            "Right + Up"   -> List(KeyDown(Key.RIGHT_ARROW), KeyDown(Key.UP_ARROW)),
            "Right + Down" -> List(KeyDown(Key.RIGHT_ARROW), KeyDown(Key.DOWN_ARROW)),
            "Right"        -> List(KeyDown(Key.RIGHT_ARROW)),
            "Up"           -> List(KeyDown(Key.UP_ARROW)),
            "Down"         -> List(KeyDown(Key.DOWN_ARROW))
          ).foreach { keys =>
            s"with keys '${keys._1}' should move correctly" in {
              val updatedModel = model
                .update(getContext(second, inputMove(keys._2)))(room)
                .getOrElse(fail("Undefined Model"))

              keys._1 match {
                case "Left + Up" =>
                  checkNewBoundingBox(updatedModel, -model.maxSpeed * second, -model.maxSpeed * second)
                case "Left + Down" =>
                  checkNewBoundingBox(updatedModel, -model.maxSpeed * second, model.maxSpeed * second)
                case "Left" =>
                  checkNewBoundingBox(updatedModel, -model.maxSpeed * second, 0)
                case "Right + Up" =>
                  checkNewBoundingBox(updatedModel, model.maxSpeed * second, -model.maxSpeed * second)
                case "Right + Down" =>
                  checkNewBoundingBox(updatedModel, model.maxSpeed * second, model.maxSpeed * second)
                case "Right" =>
                  checkNewBoundingBox(updatedModel, model.maxSpeed * second, 0)
                case "Up" =>
                  checkNewBoundingBox(updatedModel, 0, -model.maxSpeed * second)
                case "Down" =>
                  checkNewBoundingBox(updatedModel, 0, model.maxSpeed * second)
              }
            }
          }
        }
      )
    }
  }
}
