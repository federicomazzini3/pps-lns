package lns.scenes.game.anything

import scala.language.implicitConversions

import indigo.shared.{ FrameContext, Outcome }
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.ContextFixture
import lns.core.Macros.copyMacro
import lns.scenes.game.shot.ShotEvent
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

case class MyFireModel(
    boundingBox: BoundingBox,
    stats: Stats,
    fireDirection: Option[Vector2],
    fireRateTimer: Double = 0,
    shot: Option[Vector2] = None
) extends FireModel {
  type Model = MyFireModel

  def withFire(fireRateTimer: Double, shot: Option[Vector2]): MyFireModel = copyMacro
  def withStats(stats: Stats): Model                                      = copyMacro

  def computeFire(context: FrameContext[StartupData])(character: AnythingModel) = fireDirection
}

trait FireModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>
  var ShootingModel: MyFireModel    = _
  var NotShootingModel: MyFireModel = _

  val stats = Stats(
    FireDamage -> 3.5,
    FireRange  -> 500,
    FireRate   -> 2
  )

  val fireRate      = FireRate @@ stats
  val position      = Vertex(roomCenterX, roomCenterY)
  val size          = Vertex(10, 10)
  val fireDirection = Vector2(1, 0);

  override def beforeEach() = {
    ShootingModel = new MyFireModel(
      BoundingBox(position.x, position.y, size.x, size.y),
      stats,
      Some(fireDirection)
    )

    NotShootingModel = new MyFireModel(
      BoundingBox(position.x, position.y, size.x, size.y),
      stats,
      None
    )

    super.beforeEach()
  }
}

class FireModelTest extends AnyFreeSpec with FireModelFixture {
  "A FireModel with fireRate" - {
    "when not shooting" - {
      "after one frame update should" - {
        "not create ShotEvent" in {
          val updatedModelOutcome = NotShootingModel
            .update(getContext(1))(room)(character)

          assert(updatedModelOutcome.globalEventsOrNil == List())
        }
        "have no fireRateTimer countdown" in {
          val updatedModel = NotShootingModel
            .update(getContext(1))(room)(character)
            .getOrElse(fail("Undefined Model"))

          assert(updatedModel.fireRateTimer == 0)
          assert(updatedModel.isFiring() == false)
        }
      }
    }
    "when shooting" - {
      "if hasn't already shoot" - {
        "after one frame update should" - {
          "create ShotEvent" in {
            val updatedModelOutcome = ShootingModel
              .update(getContext(1))(room)(character)

            val updatedModel = updatedModelOutcome.getOrElse(fail("Undefined Model"))

            assert(
              updatedModelOutcome.globalEventsOrNil == List(
                ShotEvent(
                  Vector2(updatedModel.boundingBox.horizontalCenter, updatedModel.boundingBox.verticalCenter),
                  fireDirection
                )
              )
            )
          }
          "start a fireRateTimer countdown" in {
            val updatedModel = ShootingModel
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.fireRateTimer == fireRate)
            assert(updatedModel.isFiring() == true)
          }
        }
      }
      "if has already shoot and the fireRate countdown is active" - {
        "after one frame update should" - {
          "not create ShotEvent" in {
            val updatedModelOutcome = ShootingModel
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(1))(room)(character)

            assert(updatedModelOutcome.globalEventsOrNil == List())
          }
          "have fireRateTimer countdown active" in {
            val updatedModel = ShootingModel
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.fireRateTimer == fireRate - 1)
            assert(updatedModel.isFiring() == false)
          }
        }
      }
      "if has already shoot and the fireRate countdown is expired" - {
        "after one frame update should" - {
          "create ShotEvent" in {
            val updatedModelOutcome = ShootingModel
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(2))(room)(character)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(1))(room)(character)

            val updatedModel = updatedModelOutcome.getOrElse(fail("Undefined Model"))

            assert(
              updatedModelOutcome.globalEventsOrNil == List(
                ShotEvent(
                  Vector2(updatedModel.boundingBox.horizontalCenter, updatedModel.boundingBox.verticalCenter),
                  fireDirection
                )
              )
            )
          }
          "start new fireRateTimer countdown" in {
            val updatedModel = ShootingModel
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(2))(room)(character)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.fireRateTimer == fireRate)
            assert(updatedModel.isFiring() == true)
          }
        }
      }
    }
    "when shooting in following direction" - {
      Map(
        "Up"    -> Vector2(0, -1),
        "Right" -> Vector2(1, 0),
        "Down"  -> Vector2(0, 1),
        "Left"  -> Vector2(-1, 0)
      ).foreach { keys =>
        s"${keys._1} have correct FireState" in {
          val updatedModel =
            new MyFireModel(BoundingBox(position.x, position.y, 10, 10), stats, Some(keys._2))
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))

          keys._1 match {
            case "Up"    => assert(updatedModel.getFireState() == FireState.FIRE_UP)
            case "Right" => assert(updatedModel.getFireState() == FireState.FIRE_RIGHT)
            case "Down"  => assert(updatedModel.getFireState() == FireState.FIRE_DOWN)
            case "Left"  => assert(updatedModel.getFireState() == FireState.FIRE_LEFT)
          }

          assert(updatedModel.isFiring() == true)
        }
      }
    }
  }
}
