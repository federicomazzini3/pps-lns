package lns.scenes.game.character

import indigo.shared.constants.Key
import indigo.shared.events.InputState
import indigo.shared.input.*
import indigo.shared.events.KeyboardEvent.KeyDown
import indigoextras.geometry.{ BoundingBox, Vertex }
import indigo.shared.datatypes.Vector2
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.ContextFixture
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomModel
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }

trait CharacterModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  def inputMove = new InputState(
    Mouse.default,
    Keyboard.calculateNext(
      Keyboard.default,
      List(KeyDown(Key.LEFT_ARROW), KeyDown(Key.DOWN_ARROW))
    ),
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
}

class CharacterModelTest extends AnyFreeSpec with CharacterModelFixture {

  "A CharacterModel placed in room center" - {
    "if no keys are pressed" - {
      "after one frame update with time delta = 1s" - {
        s"should not move" in {
          val updatedModel = model
            .update(getContext(1))(room)
            .unsafeGet

          assert(
            updatedModel.boundingBox.x == centerWidth && updatedModel.boundingBox.y == centerHeight
          )
        }
      }
    }
    "if the keys left + down are pressed" - {
      "after one frame update with time delta = 1s" - {
        s"should move by (-${120},${120})" in {
          val updatedModel = model
            .update(getContext(1, inputMove))(room)
            .unsafeGet

          assert(
            updatedModel.boundingBox.x == centerWidth - model.maxSpeed && updatedModel.boundingBox.y == centerHeight + model.maxSpeed
          )
        }
      }
      "after one frame update with time delta = 2s" - {
        s"should move by (-${120 * 2},${120 * 2})" in {
          val updatedModel = model
            .update(getContext(2, inputMove))(room)
            .unsafeGet

          assert(
            updatedModel.boundingBox.x == centerWidth - model.maxSpeed * 2 && updatedModel.boundingBox.y == centerHeight + model.maxSpeed * 2
          )
        }
      }
    }
  }
}
