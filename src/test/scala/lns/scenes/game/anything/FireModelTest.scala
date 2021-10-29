package lns.scenes.game.anything

import indigo.shared.{ FrameContext, Outcome }
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.scenes.game.shot.ShotEvent
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }

case class MyFireModel(
    fireRate: Double,
    fireDirection: Option[Vector2],
    boundingBox: BoundingBox,
    fireRateTimer: Double = 0
) extends FireModel {
  type Model = MyFireModel

  override def withFire(fireRateTimer: Double): MyFireModel = copyMacro

  def computeFire(context: FrameContext[StartupData]) = fireDirection
}

trait FireModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>
  var ShootingModel: MyFireModel    = _
  var NotShootingModel: MyFireModel = _

  val fireRate      = 2
  val position      = Vertex(centerWidth, centerHeight)
  val fireDirection = Vector2(1, 0);

  override def beforeEach() = {
    ShootingModel = new MyFireModel(fireRate, Some(fireDirection), BoundingBox(position.x, position.y, 10, 10))
    NotShootingModel = new MyFireModel(fireRate, None, BoundingBox(position.x, position.y, 10, 10))

    super.beforeEach()
  }
}

class FireModelTest extends AnyFreeSpec with FireModelFixture {
  "A FireModel with fireRate" - {
    "when not shooting" - {
      "after one frame update should" - {
        "not create ShotEvent" in {
          val updatedModelOutcome = NotShootingModel
            .update(getContext(1))(room)

          assert(updatedModelOutcome.globalEventsOrNil == List())
        }
        "have no fireRateTimer countdown" in {
          val updatedModel = NotShootingModel
            .update(getContext(1))(room)
            .getOrElse(fail("Undefined Model"))

          assert(updatedModel.fireRateTimer == 0)
        }
      }
    }
    "when shooting" - {
      "if hasn't already shoot" - {
        "after one frame update should" - {
          "create ShotEvent" in {
            val updatedModelOutcome = ShootingModel
              .update(getContext(1))(room)

            assert(updatedModelOutcome.globalEventsOrNil == List(ShotEvent(position, fireDirection)))
          }
          "start a fireRateTimer countdown" in {
            val updatedModel = ShootingModel
              .update(getContext(1))(room)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.fireRateTimer == fireRate)
          }
        }
      }
      "if has already shoot and the fireRate countdown is active" - {
        "after one frame update should" - {
          "not create ShotEvent" in {
            val updatedModelOutcome = ShootingModel
              .update(getContext(1))(room)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(1))(room)

            assert(updatedModelOutcome.globalEventsOrNil == List())
          }
          "have fireRateTimer countdown" in {
            val updatedModel = ShootingModel
              .update(getContext(1))(room)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(1))(room)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.fireRateTimer == fireRate - 1)
          }
        }
      }
      "if has already shoot and the fireRate countdown is expired" - {
        "after one frame update should" - {
          "create ShotEvent" in {
            val updatedModelOutcome = ShootingModel
              .update(getContext(1))(room)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(2))(room)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(1))(room)

            assert(updatedModelOutcome.globalEventsOrNil == List(ShotEvent(position, fireDirection)))
          }
          "start fireRateTimer countdown" in {
            val updatedModel = ShootingModel
              .update(getContext(1))(room)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(2))(room)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(1))(room)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.fireRateTimer == fireRate)
          }
        }
      }
    }
  }
}
