package lns.scenes.game.anything

import scala.language.implicitConversions

import indigo.shared.FrameContext
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.ContextFixture
import lns.core.Macros.copyMacro
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

case class MyAliveModel(
    id: AnythingId,
    boundingBox: BoundingBox,
    stats: Stats,
    life: Double,
    invincibilityTimer: Double = 0
) extends AliveModel {
  type Model = MyAliveModel

  def withAlive(life: Double, invincibilityTimer: Double): MyAliveModel = copyMacro
  def withStats(stats: Stats): Model                                    = copyMacro
}

trait AliveModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  var model: MyAliveModel                = _
  var noInvincibilityModel: MyAliveModel = _

  val stats = Stats(
    MaxLife       -> 100,
    Invincibility -> 2
  )
  val statsNoInvincibility = Stats(
    MaxLife       -> 100,
    Invincibility -> 0
  )

  val maxLife       = MaxLife @@ stats
  val invincibility = Invincibility @@ stats
  val hitDamage1    = 50
  val hitDamage2    = 150

  override def beforeEach() = {
    model = new MyAliveModel(
      AnythingId.generate,
      BoundingBox(roomCenterX, roomCenterY, 10, 10),
      stats,
      MaxLife @@ stats
    )

    noInvincibilityModel = new MyAliveModel(
      AnythingId.generate,
      BoundingBox(roomCenterX, roomCenterY, 10, 10),
      statsNoInvincibility,
      MaxLife @@ statsNoInvincibility
    )

    super.beforeEach()
  }
}

class AliveModelTest extends AnyFreeSpec with AliveModelFixture {

  s"An AliveModel with life = $maxLife" - {
    "when not hit should" - {
      "after one frame update should" - {
        "maintain its life" in {
          val updatedModel = model
            .update(getContext(1))(gameContext)
            .unsafeGet

          assert(updatedModel.life == maxLife)
        }
        "have no invincibility countdown" in {
          val updatedModel: MyAliveModel = model
            .update(getContext(1))(gameContext)
            .unsafeGet

          assert(updatedModel.invincibilityTimer == 0)
        }
      }
    }
    s"when hit with damage = $hitDamage1 should" - {
      s"loose its life to ${maxLife - hitDamage1}" in {
        val updatedModel = model.hit(getContext(1), hitDamage1).unsafeGet

        assert(updatedModel.life == maxLife - hitDamage1)
      }
      s"if invincibility = ${invincibility}s" - {
        s"should have invincibilityTimer = $invincibility" in {
          val updatedModel: MyAliveModel = model.hit(getContext(1), hitDamage1).unsafeGet

          assert(updatedModel.invincibilityTimer == invincibility)
        }
        "can not be hit again" in {
          val updatedModel: MyAliveModel  = model.hit(getContext(1), hitDamage1).unsafeGet
          val updatedModel2: MyAliveModel = updatedModel.hit(getContext(1), hitDamage1).unsafeGet

          assert(updatedModel2.life == maxLife - hitDamage1)
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

          assert(updatedModel2.life == maxLife - hitDamage1 * 2)
        }
      }
      "and after one frame update should" - {
        "maintain its life" in {
          val updatedModel = model.hit(getContext(1), hitDamage1).unsafeGet

          val updatedModel2: MyAliveModel = updatedModel
            .update(getContext(1))(gameContext)
            .unsafeGet

          assert(updatedModel2.life == maxLife - hitDamage1)
        }
        "if invincibility = 0s should" - {
          "have invincibilityTimer = 0" in {
            val updatedModel = noInvincibilityModel.hit(getContext(1), hitDamage1).unsafeGet

            val updatedModel2: MyAliveModel = updatedModel
              .update(getContext(1))(gameContext)
              .unsafeGet

            assert(updatedModel2.invincibilityTimer == 0)
          }
        }
        s"if invincibility = ${invincibility}s" - {
          "after time delta = 1s" - {
            s"should have invincibilityTimer = ${invincibility - 1} " in {
              val updatedModel = model.hit(getContext(1), hitDamage1).unsafeGet

              val updatedModel2: MyAliveModel = updatedModel
                .update(getContext(1))(gameContext)
                .unsafeGet

              assert(updatedModel2.invincibilityTimer == invincibility - 1)
            }
            "can not be hit yet" in {
              val updatedModel = model.hit(getContext(1), hitDamage1).unsafeGet

              val updatedModel2: MyAliveModel = updatedModel
                .update(getContext(1))(gameContext)
                .unsafeGet

              val updatedModel3 = updatedModel2.hit(getContext(1), hitDamage1).unsafeGet

              assert(updatedModel3.life == maxLife - hitDamage1)
            }
          }
          "after time delta = 3s" - {
            "should have invincibilityTimer = 0" in {
              val updatedModel = model.hit(getContext(3), hitDamage1).unsafeGet

              val updatedModel2: MyAliveModel = updatedModel
                .update(getContext(3))(gameContext)
                .unsafeGet

              assert(updatedModel2.invincibilityTimer == 0)
            }
            "can be hit again" in {
              val updatedModel = model.hit(getContext(1), hitDamage1).unsafeGet

              val updatedModel2: MyAliveModel = updatedModel
                .update(getContext(3))(gameContext)
                .unsafeGet

              val updatedModel3: MyAliveModel = updatedModel2.hit(getContext(3), hitDamage1).unsafeGet

              assert(updatedModel3.life == maxLife - hitDamage1 * 2)
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
            .update(getContext(1))(gameContext)
            .unsafeGet

          assert(updatedModel2.life == 0)
        }
        "have invincibilityTimer = 0" in {
          val updatedModel = model.hit(getContext(1), hitDamage2).unsafeGet

          val updatedModel2: MyAliveModel = updatedModel
            .update(getContext(1))(gameContext)
            .unsafeGet

          assert(updatedModel2.invincibilityTimer == 0)
        }
      }
    }
  }
}
