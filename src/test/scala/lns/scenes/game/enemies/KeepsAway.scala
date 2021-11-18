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

case class MyKeepsAwayModel(
    id: AnythingId,
    view: () => ViewMock[MyKeepsAwayModel],
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    range: (Int, Int),
    status: Queue[EnemyStatus] = Queue((EnemyState.Attacking, 0)),
    crossable: Boolean = false,
    life: Double = 0,
    invincibilityTimer: Double = 0,
    speed: Vector2 = Vector2(0, 0)
) extends EnemyModel
    with DynamicModel
    with KeepsAway(range) {
  type Model = MyKeepsAwayModel

  def withStats(stats: Stats): Model                                          = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model                           = copyMacro
  def withAlive(life: Double, invincibilityTimer: Double): Model              = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): MyKeepsAwayModel = copyMacro
  def withSolid(crossable: Boolean): Model                                    = copyMacro
}

trait KeepsAwayModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  var model: MyKeepsAwayModel         = _
  var modelInMiddle: MyKeepsAwayModel = _
  var modelAway: MyKeepsAwayModel     = _

  val stats              = Stats(MaxSpeed -> 300)
  val maxSpeed           = MaxSpeed @@ stats
  val range              = (300, 600)
  val initialPos         = 100
  val initialPosInMiddle = 400
  val initialPosAway     = 700

  override val character: CharacterModel =
    CharacterModel.initial.withDynamic(BoundingBox(0, 0, 10, 10), Vector2(0, 0))

  override val gameContext: GameContext = GameContext(room, character)

  override def beforeEach() = {
    model = new MyKeepsAwayModel(
      AnythingId.generate,
      () => new ViewMock[MyKeepsAwayModel],
      BoundingBox(initialPos, initialPos, 10, 10),
      10,
      stats,
      range
    )
    modelInMiddle = new MyKeepsAwayModel(
      AnythingId.generate,
      () => new ViewMock[MyKeepsAwayModel],
      BoundingBox(initialPosInMiddle, initialPosInMiddle, 10, 10),
      10,
      stats,
      range
    )
    modelAway = new MyKeepsAwayModel(
      AnythingId.generate,
      () => new ViewMock[MyKeepsAwayModel],
      BoundingBox(initialPosAway, initialPosAway, 10, 10),
      10,
      stats,
      range
    )

    super.beforeEach()
  }
}

class KeepsAwayTest extends AnyFreeSpec with KeepsAwayModelFixture {
  "A KeepsAway" - {
    s"placed in ($initialPos,$initialPos) and a Character placed in (0,0)" - {
      "after one frame update with" - {
        "time delta 1s having" - {
          s"max speed ($maxSpeed,$maxSpeed) should" - {
            "be moving" in {
              val updatedModel: MyKeepsAwayModel = model
                .update(getContext(1))(gameContext)
                .getOrElse(fail("Undefined Model"))

              assert(updatedModel.isMoving() == true)
            }
            "move by $maxSpeed" in {
              val updatedModel: MyKeepsAwayModel = model
                .update(getContext(1))(gameContext)
                .getOrElse(fail("Undefined Model"))

              val distance = updatedModel.getPosition().distanceTo(Vector2(initialPos, initialPos))
              assert(distance < maxSpeed + 0.001 && distance > maxSpeed - 0.001)
            }
            s"move in direction (1,1)" in {
              val updatedModel: MyKeepsAwayModel = model
                .update(getContext(1))(gameContext)
                .getOrElse(fail("Undefined Model"))

              val direction = (Vector2(initialPos, initialPos) - updatedModel.getPosition()).normalise
              assert(direction.x < 0 && direction.x == direction.y)
            }
          }
        }
      }
    }
    s"placed in ($initialPosInMiddle,$initialPosInMiddle) and a Character placed in (0,0)" - {
      "after one frame update with" - {
        "time delta 1s should" - {
          "not be moving" in {
            val updatedModel: MyKeepsAwayModel = modelInMiddle
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.isMoving() == false)
          }
          "maintains position" in {
            val updatedModel: MyKeepsAwayModel = modelInMiddle
              .update(getContext(1))(gameContext)
              .getOrElse(fail("Undefined Model"))

            assert(updatedModel.getPosition() == Vector2(initialPosInMiddle, initialPosInMiddle))
          }
        }
      }
    }
    s"placed in ($initialPosAway,$initialPosAway) and a Character placed in (0,0)" - {
      "after one frame update with" - {
        "time delta 1s having" - {
          s"max speed ($maxSpeed,$maxSpeed) should" - {
            "be moving" in {
              val updatedModel: MyKeepsAwayModel = modelAway
                .update(getContext(1))(gameContext)
                .getOrElse(fail("Undefined Model"))

              assert(updatedModel.isMoving() == true)
            }
            "move by $maxSpeed" in {
              val updatedModel: MyKeepsAwayModel = modelAway
                .update(getContext(1))(gameContext)
                .getOrElse(fail("Undefined Model"))

              val distance = updatedModel.getPosition().distanceTo(Vector2(initialPosAway, initialPosAway))
              assert(distance < maxSpeed + 0.001 && distance > maxSpeed - 0.001)
            }
            s"move in direction (-1,-1)" in {
              val updatedModel: MyKeepsAwayModel = modelAway
                .update(getContext(1))(gameContext)
                .getOrElse(fail("Undefined Model"))

              val direction = (updatedModel.getPosition() - Vector2(initialPosAway, initialPosAway)).normalise
              assert(direction.x < 0 && direction.x == direction.y)
            }
          }
        }
      }
    }
  }
}
