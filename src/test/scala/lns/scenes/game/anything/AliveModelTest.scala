package lns.scenes.game.anything

import indigo.shared.FrameContext
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Macros.copyMacro
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

trait AliveModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

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
      "after one frame update should" - {
        "maintain its life" in {
          val updatedModel = model
            .update(getContext(1))(room)
            .unsafeGet

          assert(updatedModel.life == startLife)
        }
        "have no invincibility countdown" in {
          val updatedModel: MyAliveModel = model
            .update(getContext(1))(room)
            .unsafeGet

          assert(updatedModel.invincibilityTimer == 0)
        }
      }
    }
    s"when hit with damage = $hitDamage1 should" - {
      s"loose its life to ${startLife - hitDamage1}" in {
        val updatedModel = model.hit(getContext(1), hitDamage1).unsafeGet

        assert(updatedModel.life == startLife - hitDamage1)
      }
      s"if invincibility = ${invincibility}s" - {
        s"should have invincibilityTimer = $invincibility" in {
          val updatedModel: MyAliveModel = model.hit(getContext(1), hitDamage1).unsafeGet

          assert(updatedModel.invincibilityTimer == invincibility)
        }
        "can not be hit again" in {
          val updatedModel: MyAliveModel  = model.hit(getContext(1), hitDamage1).unsafeGet
          val updatedModel2: MyAliveModel = updatedModel.hit(getContext(1), hitDamage1).unsafeGet

          assert(updatedModel2.life == startLife - hitDamage1)
        }
      }
      "if invincibility = 0s" - {
        "should have invincibilityTimer = 0" in {
          val updatedModel: MyAliveModel = noInvincibilityModel.hit(getContext(1), hitDamage1).unsafeGet

          assert(updatedModel.invincibilityTimer == 0)
        }
        "can be hit again" in {
          val updatedModel: MyAliveModel  = noInvincibilityModel.hit(getContext(1), hitDamage1).unsafeGet
          val updatedModel2: MyAliveModel = updatedModel.hit(getContext(1), hitDamage1).unsafeGet

          assert(updatedModel2.life == startLife - hitDamage1 * 2)
        }
      }
      "and after one frame update should" - {
        "maintain its life" in {
          val updatedModel = model.hit(getContext(1), hitDamage1).unsafeGet

          val updatedModel2: MyAliveModel = updatedModel
            .update(getContext(1))(room)
            .unsafeGet

          assert(updatedModel2.life == startLife - hitDamage1)
        }
        "if invincibility = 0s should" - {
          "have invincibilityTimer = 0" in {
            val updatedModel = noInvincibilityModel.hit(getContext(1), hitDamage1).unsafeGet

            val updatedModel2: MyAliveModel = updatedModel
              .update(getContext(1))(room)
              .unsafeGet

            assert(updatedModel2.invincibilityTimer == 0)
          }
        }
        s"if invincibility = ${invincibility}s" - {
          "after time delta = 1s" - {
            s"should have invincibilityTimer = ${invincibility - 1} " in {
              val updatedModel = model.hit(getContext(1), hitDamage1).unsafeGet

              val updatedModel2: MyAliveModel = updatedModel
                .update(getContext(1))(room)
                .unsafeGet

              assert(updatedModel2.invincibilityTimer == invincibility - 1)
            }
            "can not be hit yet" in {
              val updatedModel = model.hit(getContext(1), hitDamage1).unsafeGet

              val updatedModel2: MyAliveModel = updatedModel
                .update(getContext(1))(room)
                .unsafeGet

              val updatedModel3 = updatedModel2.hit(getContext(1), hitDamage1).unsafeGet

              assert(updatedModel3.life == startLife - hitDamage1)
            }
          }
          "after time delta = 3s" - {
            "should have invincibilityTimer = 0" in {
              val updatedModel = model.hit(getContext(3), hitDamage1).unsafeGet

              val updatedModel2: MyAliveModel = updatedModel
                .update(getContext(3))(room)
                .unsafeGet

              assert(updatedModel2.invincibilityTimer == 0)
            }
            "can be hit again" in {
              val updatedModel = model.hit(getContext(1), hitDamage1).unsafeGet

              val updatedModel2: MyAliveModel = updatedModel
                .update(getContext(3))(room)
                .unsafeGet

              val updatedModel3: MyAliveModel = updatedModel2.hit(getContext(3), hitDamage1).unsafeGet

              assert(updatedModel3.life == startLife - hitDamage1 * 2)
            }
          }
        }
      }
    }
    s"when hit with damage = $hitDamage2 should" - {
      "loose its life to 0" in {
        val updatedModel = model.hit(getContext(1), hitDamage2).unsafeGet

        assert(updatedModel.life == 0)
      }
      "have invincibilityTimer = 0" in {
        val updatedModel: MyAliveModel = model.hit(getContext(1), hitDamage2).unsafeGet

        assert(updatedModel.invincibilityTimer == 0)
      }
      "can not be hit again" in {
        val updatedModel: MyAliveModel  = model.hit(getContext(1), hitDamage2).unsafeGet
        val updatedModel2: MyAliveModel = updatedModel.hit(getContext(1), hitDamage1).unsafeGet

        assert(updatedModel.life == 0)
      }
      "after one frame update should" - {
        "maintain its life = 0" in {
          val updatedModel = model.hit(getContext(1), hitDamage2).unsafeGet

          val updatedModel2: MyAliveModel = updatedModel
            .update(getContext(1))(room)
            .unsafeGet

          assert(updatedModel2.life == 0)
        }
        "have invincibilityTimer = 0" in {
          val updatedModel = model.hit(getContext(1), hitDamage2).unsafeGet

          val updatedModel2: MyAliveModel = updatedModel
            .update(getContext(1))(room)
            .unsafeGet

          assert(updatedModel2.invincibilityTimer == 0)
        }
      }
    }
  }
}
