package lns.scenes.game.enemy

import scala.language.implicitConversions

import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.core.{ ContextFixture, ViewMock }
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ AnythingId, AnythingModel, FireModel }
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.shot.*
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }

import scala.collection.immutable.Queue

case class MyFiringModel(
    id: AnythingId,
    view: () => ViewMock[MyFiringModel],
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Attacking, 0)),
    crossable: Boolean = false,
    life: Double = 0,
    invincibilityTimer: Double = 0,
    fireRateTimer: Double = 0,
    shot: Option[Vector2] = None
) extends EnemyModel
    with FireModel
    with FiresContinuously {
  type Model = MyFiringModel

  val shotView   = () => new SingleShotView() with ShotBlue
  val shotOffset = 5

  def withStats(stats: Stats): Model                                = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model                 = copyMacro
  def withAlive(life: Double, invincibilityTimer: Double): Model    = copyMacro
  def withFire(fireRateTimer: Double, shot: Option[Vector2]): Model = copyMacro
  def withSolid(crossable: Boolean): Model                          = copyMacro
}

trait FiringModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>
  var model: MyFiringModel = _

  val stats = Stats(
    FireDamage -> 3.5,
    FireRange  -> 500,
    FireRate   -> 1,
    FireSpeed  -> 800
  )

  val shotStats  = Stats.createShot(stats)
  val fireRate   = FireRate @@ stats
  val initialPos = 100

  val tests: List[(CharacterModel, Vector2)] =
    List(Vector2(100, 0), Vector2(200, 100), Vector2(100, 200), Vector2(0, 100)).map(p =>
      (
        character.withDynamic(BoundingBox(p.x, p.y, 10, 10), Vector2(0, 0)),
        (p - Vector2(initialPos, initialPos)).normalise
      )
    )

  override def beforeEach() = {
    model = new MyFiringModel(
      AnythingId.generate,
      () => new ViewMock[MyFiringModel],
      BoundingBox(initialPos, initialPos, 10, 10),
      10,
      stats
    )

    super.beforeEach()
  }
}

class FiresContinuouslyTest extends AnyFreeSpec with FiringModelFixture {
  s"A FiresContinuously placed in ($initialPos,$initialPos) with fireRate $fireRate  and a Character placed " - {
    tests.foreach { test =>
      val characterPos = test._1.getPosition()
      s"in (${characterPos.x},${characterPos.y})" - {
        "after one frame update should" - {
          "fire a shot in character direction" in {
            val updatedModelOutcome =
              model.update(getContext(1))(GameContext(room, test._1))
            val updatedModel =
              updatedModelOutcome.getOrElse(fail("Undefined Model"))

            val updatedPosition = Vertex(
              updatedModel.boundingBox.horizontalCenter,
              updatedModel.boundingBox.top + updatedModel.shotOffset
            )

            val result = updatedModelOutcome.globalEventsOrNil

            assert(result.length == 1)
            result.foreach {
              case ShotEvent(shot) =>
                assert(shot.boundingBox.position == updatedPosition)
                assert(shot.direction == test._2)
                assert(shot.owner == updatedModel.id)
                assert(shot.stats == shotStats)
              case _ => fail("Undefined Shotevent")
            }
          }
          s"and after 0.5s should" - {
            "not fire a shot " in {
              val updatedModel = model
                .update(getContext(1))(GameContext(room, test._1))
                .getOrElse(fail("Undefined Model"))

              val updatedModelOutcome = updatedModel.update(getContext(0.5))(GameContext(room, test._1))
              assert(updatedModelOutcome.globalEventsOrNil == List())
            }
          }
          s"and after two frames in 0.9s should" - {
            "not fire a shot " in {
              val updatedModel = model
                .update(getContext(1))(GameContext(room, test._1))
                .getOrElse(fail("Undefined Model"))
                .update(getContext(0.8))(GameContext(room, test._1))
                .getOrElse(fail("Undefined Model"))

              val updatedModelOutcome = updatedModel.update(getContext(0.1))(GameContext(room, test._1))
              assert(updatedModelOutcome.globalEventsOrNil == List())
            }
          }
          s"but after a frame in 1.5s should" - {
            "fire another shot in character direction" in {
              val updatedModel = model
                .update(getContext(1))(GameContext(room, test._1))
                .getOrElse(fail("Undefined Model"))
              val updatedModelOutcome = updatedModel.update(getContext(1.5))(GameContext(room, test._1))
              val updatedModelNew     = updatedModelOutcome.getOrElse(fail("Undefined Model"))

              val updatedPosition = Vertex(
                updatedModelNew.boundingBox.horizontalCenter,
                updatedModelNew.boundingBox.top + updatedModelNew.shotOffset
              )

              val result = updatedModelOutcome.globalEventsOrNil

              assert(result.length == 1)
              result.foreach {
                case ShotEvent(shot) =>
                  assert(shot.boundingBox.position == updatedPosition)
                  assert(shot.direction == test._2)
                  assert(shot.owner == updatedModel.id)
                  assert(shot.stats == shotStats)
                case _ => fail("Undefined Shotevent")
              }

            }
          }
        }
      }
    }
  }
}
