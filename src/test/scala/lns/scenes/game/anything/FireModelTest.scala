package lns.scenes.game.anything

import scala.language.implicitConversions

import indigo.shared.{ FrameContext, Outcome }
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.{ ContextFixture, ViewMock }
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.AnythingId
import lns.scenes.game.shots.*
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }

case class MyFireModel(
    id: AnythingId,
    view: () => ViewMock[MyFireModel],
    boundingBox: BoundingBox,
    stats: Stats,
    fireDirection: Option[List[Vector2]],
    fireRateTimer: Double = 0,
    shots: Option[List[Vector2]] = None
) extends FireModel {
  type Model = MyFireModel

  val shotView   = () => new SingleShotView() with ShotBlue
  val shotOffset = 0

  def withFire(fireRateTimer: Double, shots: Option[List[Vector2]]): MyFireModel = copyMacro
  def withStats(stats: Stats): Model                                             = copyMacro

  def computeFire(context: FrameContext[StartupData])(gameContext: GameContext) = fireDirection
}

trait FireModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>
  var ShootingModel: MyFireModel         = _
  var MultipleShootingModel: MyFireModel = _
  var NotShootingModel: MyFireModel      = _

  val stats = Stats(
    FireDamage -> 3.5,
    FireRange  -> 500,
    FireRate   -> 2,
    FireSpeed  -> 800
  )

  val shotStats             = Stats.createShot(stats)
  val fireRate              = FireRate @@ stats
  val position              = Vector2(roomCenterX, roomCenterY)
  val size                  = Vector2(10, 10)
  val fireDirection         = List(Vector2(1, 0));
  val multipleFireDirection = List(Vector2(1, 0), Vector2(0, 1));

  override def beforeEach() = {
    ShootingModel = new MyFireModel(
      AnythingId.generate,
      () => new ViewMock[MyFireModel],
      BoundingBox(position, size),
      stats,
      Some(fireDirection)
    )

    MultipleShootingModel = new MyFireModel(
      AnythingId.generate,
      () => new ViewMock[MyFireModel],
      BoundingBox(position, size),
      stats,
      Some(multipleFireDirection)
    )

    NotShootingModel = new MyFireModel(
      AnythingId.generate,
      () => new ViewMock[MyFireModel],
      BoundingBox(position, size),
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
            .update(getContext(1))(gameContext)

          assert(updatedModelOutcome.globalEventsOrNil == List())
        }
        "have no fireRateTimer countdown" in {
          val updatedModel = NotShootingModel
            .update(getContext(1))(gameContext)
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
              .update(getContext(1))(gameContext)
            val updatedModel =
              updatedModelOutcome.getOrElse(fail("Undefined Model"))
            val updatedPosition =
              Vertex(updatedModel.boundingBox.horizontalCenter, updatedModel.boundingBox.top + updatedModel.shotOffset)
            val result = updatedModelOutcome.globalEventsOrNil

            assert(result.length == 1)
            result.foreach {
              case ShotEvent(shot) =>
                assert(shot.boundingBox.position == updatedPosition)
                assert(shot.direction == fireDirection(0))
                assert(shot.owner == updatedModel.id)
                assert(shot.stats == shotStats)
              case _ => fail("Undefined Shotevent")
            }
          }
          "start a fireRateTimer countdown" in {
            val updatedModel = ShootingModel
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.fireRateTimer == fireRate)
            assert(updatedModel.isFiring() == true)
          }
          "create Multiple ShotEvent" in {
            val updatedModelOutcome = MultipleShootingModel
              .update(getContext(1))(gameContext)
            val updatedModel =
              updatedModelOutcome.getOrElse(fail("Undefined Model"))
            val updatedPosition =
              Vertex(updatedModel.boundingBox.horizontalCenter, updatedModel.boundingBox.top + updatedModel.shotOffset)
            val result = updatedModelOutcome.globalEventsOrNil

            assert(result.length == 2)
            result.zipWithIndex.foreach {
              case (ShotEvent(shot), i) =>
                assert(shot.boundingBox.position == updatedPosition)
                assert(shot.direction == multipleFireDirection(i))
                assert(shot.owner == updatedModel.id)
                assert(shot.stats == shotStats)
              case _ => fail("Undefined Shotevent")
            }
          }
        }
      }
      "if has already shoot and the fireRate countdown is active" - {
        "after one frame update should" - {
          "not create ShotEvent" in {
            val updatedModelOutcome = ShootingModel
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(1))(gameContext)

            assert(updatedModelOutcome.globalEventsOrNil == List())
          }
          "have fireRateTimer countdown active" in {
            val updatedModel = ShootingModel
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.fireRateTimer == fireRate - 1)
          }
        }
      }
      "if has already shoot and the fireRate countdown is expired" - {
        "after one frame update should" - {
          "create ShotEvent" in {
            val updatedModelOutcome = ShootingModel
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(fireRate + 2))(gameContext)
            val updatedModel =
              updatedModelOutcome.getOrElse(fail("Undefined Model"))
            val updatedPosition =
              Vertex(updatedModel.boundingBox.horizontalCenter, updatedModel.boundingBox.top + updatedModel.shotOffset)
            val result = updatedModelOutcome.globalEventsOrNil

            assert(result.length == 1)
            result.foreach {
              case ShotEvent(shot) =>
                assert(shot.boundingBox.position == updatedPosition)
                assert(shot.direction == fireDirection(0))
                assert(shot.owner == updatedModel.id)
                assert(shot.stats == shotStats)
              case _ => fail("Undefined Shotevent")
            }
          }
          "start new fireRateTimer countdown" in {
            val updatedModel = ShootingModel
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))
              .update(getContext(fireRate + 2))(gameContext)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.fireRateTimer == fireRate)
          }
        }
      }
    }
    "when shooting in following direction" - {
      Map(
        "Up"       -> List(Vector2(0, -1)),
        "Right"    -> List(Vector2(1, 0)),
        "Down"     -> List(Vector2(0, 1)),
        "Left"     -> List(Vector2(-1, 0)),
        "Multiple" -> List(Vector2(-1, 0), Vector2(1, 0))
      ).foreach { keys =>
        s"${keys._1} have correct FireState" in {
          val updatedModel =
            new MyFireModel(
              AnythingId.generate,
              () => new ViewMock[MyFireModel],
              BoundingBox(position.x, position.y, 10, 10),
              stats,
              Some(keys._2)
            )
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))

          keys._1 match {
            case "Up"       => assert(updatedModel.getFireState() == FireState.FIRE_UP)
            case "Right"    => assert(updatedModel.getFireState() == FireState.FIRE_RIGHT)
            case "Down"     => assert(updatedModel.getFireState() == FireState.FIRE_DOWN)
            case "Left"     => assert(updatedModel.getFireState() == FireState.FIRE_LEFT)
            case "Multiple" => assert(updatedModel.getFireState() == FireState.MULTIPLE_FIRE)
          }

          assert(updatedModel.isFiring() == true)
        }
      }
    }
  }
}
