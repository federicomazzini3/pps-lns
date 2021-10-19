package lns.scenes.game.anything

import indigo.platform.assets.DynamicText
import indigo.shared.{ AnimationsRegister, BoundaryLocator, FontRegister, FrameContext }
import indigo.shared.datatypes.{ Rectangle, Vector2 }
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.time.{ GameTime, Seconds }
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomModel
import org.junit.jupiter.api.Assertions.assertFalse
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }

case class MyAliveModel(
    boundingBox: BoundingBox,
    life: Int,
    invincibility: Double,
    invincibilityTimer: Double = 0
) extends AliveModel {
  type Model = MyAliveModel

  override def withAlive(life: Int, invincibilityTimer: Double): MyAliveModel = copyMacro
}

trait AliveModelFixture extends BeforeAndAfterEach { this: Suite =>

  val width           = 1600
  val height          = 900
  val centerWidth     = width / 2
  val centerHeight    = height / 2
  val startupData     = StartupData(screenDimensions = Rectangle(0, 0, width, height))
  val inputState      = InputState.default
  val dice            = Dice.fromSeed(1000)
  val boundaryLocator = new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText)
  val room            = RoomModel.initial(startupData)

  val context1 = new FrameContext[StartupData](
    GameTime.withDelta(Seconds(10), Seconds(1)),
    dice,
    inputState,
    boundaryLocator,
    startupData
  )

  val context2 = new FrameContext[StartupData](
    GameTime.withDelta(Seconds(10), Seconds(3)),
    dice,
    inputState,
    boundaryLocator,
    startupData
  )

  var model: MyAliveModel                = _
  var noInvincibilityModel: MyAliveModel = _

  val startLife     = 100
  val hitDamage1    = 50
  val hitDamage2    = 150
  val invincibility = 2

  override def beforeEach() = {
    model = new MyAliveModel(BoundingBox(centerWidth, centerHeight, 10, 10), startLife, invincibility)
    noInvincibilityModel = new MyAliveModel(BoundingBox(centerWidth, centerHeight, 10, 10), startLife, 0)

    super.beforeEach()
  }
}

class AliveModelTest extends AnyFreeSpec with AliveModelFixture {

  s"An AliveModel with life = $startLife" - {
    "when not hit should" - {
      "after one frame update" - {
        "maintain its life" in {
          val updatedModel = model
            .update(context1)(room)
            .unsafeGet

          assert(updatedModel.life == startLife)
        }
        "have no invincibility countdown" in {
          val updatedModel: MyAliveModel = model
            .update(context1)(room)
            .unsafeGet

          assert(updatedModel.invincibilityTimer == 0)
        }
      }
    }
    s"when hit with damage = $hitDamage1 should" - {
      "loose its life" in {
        val updatedModel = model.hit(context1, hitDamage1).unsafeGet

        assert(updatedModel.life == startLife - hitDamage1)
      }
      s"have invincibilityTimer = $invincibility" in {
        val updatedModel: MyAliveModel = model.hit(context1, hitDamage1).unsafeGet

        assert(updatedModel.invincibilityTimer == invincibility)
      }
      "after one frame update should" - {
        "maintain its life" in {
          val updatedModel = model.hit(context1, hitDamage1).unsafeGet

          val updatedModel2: MyAliveModel = updatedModel
            .update(context1)(room)
            .unsafeGet

          assert(updatedModel2.life == startLife - hitDamage1)
        }
        "if no invincibility is set" - {
          "have invincibilityTimer = 0 " in {
            val updatedModel = noInvincibilityModel.hit(context1, hitDamage1).unsafeGet

            val updatedModel2: MyAliveModel = updatedModel
              .update(context1)(room)
              .unsafeGet

            assert(updatedModel2.invincibilityTimer == 0)
          }
        }
        s"if invincibility = $invincibility" - {
          s"have invincibilityTimer = ${invincibility - 1} if time delta = 1" in {
            val updatedModel = model.hit(context1, hitDamage1).unsafeGet

            val updatedModel2: MyAliveModel = updatedModel
              .update(context1)(room)
              .unsafeGet

            assert(updatedModel2.invincibilityTimer == invincibility - 1)
          }
          s"have invincibilityTimer = 0 if time delta = 3" in {
            val updatedModel = model.hit(context2, hitDamage1).unsafeGet

            val updatedModel2: MyAliveModel = updatedModel
              .update(context2)(room)
              .unsafeGet

            assert(updatedModel2.invincibilityTimer == 0)
          }
        }
      }
    }
    s"when hit with damage = $hitDamage2 should" - {
      "loose its life to 0" in {
        val updatedModel = model.hit(context1, hitDamage2).unsafeGet

        assert(updatedModel.life == 0)
      }
      s"have invincibilityTimer = 0" in {
        val updatedModel: MyAliveModel = model.hit(context1, hitDamage2).unsafeGet

        assert(updatedModel.invincibilityTimer == 0)
      }
      "after one frame update should" - {
        "maintain its life to 0" in {
          val updatedModel = model.hit(context1, hitDamage2).unsafeGet

          val updatedModel2: MyAliveModel = updatedModel
            .update(context1)(room)
            .unsafeGet

          assert(updatedModel2.life == 0)
        }
        "have invincibilityTimer = 0 " in {
          val updatedModel = model.hit(context1, hitDamage2).unsafeGet

          val updatedModel2: MyAliveModel = updatedModel
            .update(context1)(room)
            .unsafeGet

          assert(updatedModel2.invincibilityTimer == 0)
        }
      }
    }
  }
}
