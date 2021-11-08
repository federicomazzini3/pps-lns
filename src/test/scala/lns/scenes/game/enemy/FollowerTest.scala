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

case class MyFollowerModel(
    boundingBox: BoundingBox,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Attacking, 0)),
    life: Int = 0,
    invincibilityTimer: Double = 0,
    speed: Vector2 = Vector2(0, 0)
) extends EnemyModel
    with DynamicModel
    with Follower {
  type Model = MyFollowerModel

  def withStats(stats: Stats): Model                               = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model                = copyMacro
  def withAlive(life: Int, invincibilityTimer: Double): Model      = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro
}

trait FollowerModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  var model: MyFollowerModel = _

  val stats      = Stats(MaxSpeed -> 2)
  val maxSpeed   = MaxSpeed @@ stats
  val initialPos = 100

  override val character: CharacterModel =
    CharacterModel.initial.withDynamic(BoundingBox(0, 0, 10, 10), Vector2(0, 0))

  override def beforeEach() = {
    model = new MyFollowerModel(BoundingBox(initialPos, initialPos, 10, 10), stats)

    super.beforeEach()
  }
}

class FollowerTest extends AnyFreeSpec with FollowerModelFixture {
  s"A Follower placed in ($initialPos,$initialPos) and a Character placed in (0,0)" - {
    "after one frame update with" - {
      "time delta 1s having" - {
        s"max speed $maxSpeed should" - {
          "be moving" in {
            val updatedModel: MyFollowerModel = model
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.isMoving() == true)
          }
          s"move by $maxSpeed" in {
            val updatedModel: MyFollowerModel = model
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))

            val distance = updatedModel.getPosition().distanceTo(Vector2(initialPos, initialPos))
            assert(distance < maxSpeed + 0.001 && distance > maxSpeed - 0.001)
          }
          "move in direction (-1,-1)" in {
            val updatedModel: MyFollowerModel = model
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))

            val direction = (updatedModel.getPosition() - Vector2(initialPos, initialPos)).normalise
            assert(direction.x < 0 && direction.x == direction.y)
          }
        }
      }
    }
  }
}
