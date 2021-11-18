package lns.scenes.game.enemies

import scala.language.implicitConversions

import indigo.*
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.core.{ ContextFixture, ViewMock }
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ AnythingId, AnythingModel, DynamicModel }
import lns.scenes.game.characters.CharacterModel
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }

import scala.collection.immutable.Queue

case class MyFollowerModel(
    id: AnythingId,
    view: () => ViewMock[MyFollowerModel],
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Attacking, 0)),
    crossable: Boolean = false,
    life: Double = 0,
    invincibilityTimer: Double = 0,
    speed: Vector2 = Vector2(0, 0)
) extends EnemyModel
    with DynamicModel
    with Follower {
  type Model = MyFollowerModel

  def withStats(stats: Stats): Model                               = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model                = copyMacro
  def withAlive(life: Double, invincibilityTimer: Double): Model   = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro
  def withSolid(crossable: Boolean): Model                         = copyMacro
}

trait FollowerModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  var model: MyFollowerModel = _

  val stats      = Stats(MaxSpeed -> 2)
  val maxSpeed   = MaxSpeed @@ stats
  val initialPos = 100

  override val character: CharacterModel =
    CharacterModel.initial.withDynamic(BoundingBox(0, 0, 10, 10), Vector2(0, 0))

  override val gameContext: GameContext = GameContext(room, character)

  override def beforeEach() = {
    model = new MyFollowerModel(
      AnythingId.generate,
      () => new ViewMock[MyFollowerModel],
      BoundingBox(initialPos, initialPos, 10, 10),
      10,
      stats
    )

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
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.isMoving() == true)
          }
          s"move by $maxSpeed" in {
            val updatedModel: MyFollowerModel = model
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))

            val distance = updatedModel.getPosition().distanceTo(Vector2(initialPos, initialPos))
            assert(distance < maxSpeed + 0.001 && distance > maxSpeed - 0.001)
          }
          "move in direction (-1,-1)" in {
            val updatedModel: MyFollowerModel = model
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))

            val direction = (updatedModel.getPosition() - Vector2(initialPos, initialPos)).normalise
            assert(direction.x < 0 && direction.x == direction.y)
          }
        }
      }
    }
  }
}
