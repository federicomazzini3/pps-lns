package lns.scenes.game.enemy

import scala.language.implicitConversions

import indigo.shared.FrameContext
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.core.ContextFixture
import lns.scenes.game.anything.{ AnythingModel, DynamicModel }
import lns.scenes.game.room.RoomModel
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*
import scala.collection.immutable.Queue

case class MyTravellerModel(
    boundingBox: BoundingBox,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Attacking, 0)),
    val enabled: Boolean = true,
    life: Int = 0,
    invincibilityTimer: Double = 0,
    speed: Vector2 = Vector2(0, 0),
    path: Queue[Vector2] = Queue.empty
) extends EnemyModel
    with Traveller {
  type Model = MyTravellerModel

  def withStats(stats: Stats): Model                               = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model                = copyMacro
  def withAlive(life: Int, invincibilityTimer: Double): Model      = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro
  def withTraveller(path: Queue[Vector2]): Model                   = copyMacro
}

trait TravellerModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  var stoppedModel: MyTravellerModel = _
  var movingModel: MyTravellerModel  = _

  val stats      = Stats(MaxSpeed -> 2)
  val maxSpeed   = MaxSpeed @@ stats
  val initialPos = 100
  val path       = Queue(Vector2(150, 150), Vector2(200, 150))

  val firstDirection = (path.head - Vector2(initialPos, initialPos)).normalise

  override val character: CharacterModel =
    CharacterModel.initial.withDynamic(BoundingBox(0, 0, 10, 10), Vector2(0, 0))

  override def beforeEach() = {
    stoppedModel = new MyTravellerModel(BoundingBox(initialPos, initialPos, 10, 10), stats)
    movingModel = new MyTravellerModel(BoundingBox(initialPos, initialPos, 10, 10), stats, path = path)

    super.beforeEach()
  }
}

class TravellerTest extends AnyFreeSpec with TravellerModelFixture {
  s"A Traveller placed in ($initialPos,$initialPos)" - {
    "having no path" - {
      "after one frame update with" - {
        "time delta 1s having" - {
          s"max speed $maxSpeed should" - {
            "not move" in {
              val updatedModel: MyTravellerModel = stoppedModel
                .update(getContext(1))(room)(character)
                .getOrElse(fail("Undefined Model"))

              assert(updatedModel.isMoving() == false)
            }
          }
        }
      }
    }
    s"having a path $path" - {
      "after one frame update with" - {
        "time delta 1s having" - {
          s"max speed $maxSpeed should" - {
            "be moving" in {
              val updatedModel: MyTravellerModel = movingModel
                .update(getContext(1))(room)(character)
                .getOrElse(fail("Undefined Model"))

              assert(updatedModel.isMoving() == true)
            }
            s"move by $maxSpeed" in {
              val updatedModel: MyTravellerModel = movingModel
                .update(getContext(1))(room)(character)
                .getOrElse(fail("Undefined Model"))

              val distance = updatedModel.getPosition().distanceTo(Vector2(initialPos, initialPos))
              assert(distance < maxSpeed + 0.001 && distance > maxSpeed - 0.001)
            }
            s"move in direction $firstDirection" in {
              val updatedModel: MyTravellerModel = movingModel
                .update(getContext(1))(room)(character)
                .getOrElse(fail("Undefined Model"))

              val direction = (updatedModel.getPosition() - Vector2(initialPos, initialPos)).normalise
              assert(direction ~== firstDirection)
            }
          }
        }
      }
    }
  }
}
