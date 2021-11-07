package lns.scenes.game.enemy

import scala.language.implicitConversions

import indigo.*
import indigo.shared.FrameContext
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.core.ContextFixture
import lns.scenes.game.anything.{ AnythingModel, FireModel }
import lns.scenes.game.room.RoomModel
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.shot.ShotEvent
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

case class MyFiringModel(
    boundingBox: BoundingBox,
    stats: Stats,
    status: EnemyState = EnemyState.Attacking,
    life:Int = 0,
    invincibilityTimer:Double = 0,
    fireRateTimer: Double = 0,
    shot: Option[Vector2] = None
) extends EnemyModel
    with FireModel
    with FiresContinuously {
  type Model = MyFiringModel

  def withStats(stats: Stats): Model                                 = copyMacro
  def withStatus(status: EnemyState): Model                        = copyMacro
  def withAlive(life: Int, invincibilityTimer: Double): Model      = copyMacro
  def withFire(fireRateTimer: Double, shot: Option[Vector2]): Model = copyMacro
}

trait FiringModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  var model: MyFiringModel = _

  val stats = Stats(
    FireDamage -> 3.5,
    FireRange  -> 500,
    FireRate   -> 1
  )

  val fireRate = FireRate @@ stats
  val initialPos           = 100

  val tests: List[(CharacterModel, Vector2)] =
    List(Vector2(100, 0), Vector2(200, 100), Vector2(100, 200), Vector2(0, 100)).map(p =>
      (character.withDynamic(BoundingBox(p.x, p.y, 10, 10), Vector2(0, 0)), (p - Vector2(initialPos, initialPos)).normalise)
    )

  override def beforeEach() = {
    model = new MyFiringModel(BoundingBox(initialPos, initialPos, 10, 10), stats)

    super.beforeEach()
  }
}

class FiresContinuouslyTest extends AnyFreeSpec with FiringModelFixture {
  s"A FiresContinuously placed in ($initialPos,$initialPos) and a Character placed " - {
    tests.foreach(test =>
      val characterPos = test._1.getPosition()
      s"in (${characterPos.x},${characterPos.y})" - {
        "after one frame update should" - {
          "fire a shot in character direction" in {
            val outcome: Outcome[MyFiringModel] = model.update(getContext(1))(room)(test._1)

            assert(outcome.globalEventsOrNil == List(ShotEvent(model.boundingBox.center.toVector2, test._2)))
          }
          s"and after 0.5s and fireRate ${fireRate} should" - {
            "not fire a shot " in {
              val updatedModel: MyFiringModel = model
                .update(getContext(1))(room)(test._1).getOrElse(fail("Undefined Model"))

              val outcome: Outcome[MyFiringModel] = updatedModel.update(getContext(0.5))(room)(test._1)
              assert(outcome.globalEventsOrNil == List())
            }
          }
          s"and after two frames in 0.9s and fireRate ${fireRate} should" - {
            "not fire a shot " in {
              val updatedModel: MyFiringModel = model
                .update(getContext(1))(room)(test._1).getOrElse(fail("Undefined Model"))
                .update(getContext(0.8))(room)(test._1).getOrElse(fail("Undefined Model"))

              val outcome: Outcome[MyFiringModel] = updatedModel.update(getContext(0.1))(room)(test._1)
              assert(outcome.globalEventsOrNil == List())
            }
          }
          s"but after two frames in 1.5s and fireRate ${fireRate} should" - {
            "fire another shot in character direction" in {
              val updatedModel: MyFiringModel = model
                .update(getContext(1))(room)(test._1).getOrElse(fail("Undefined Model"))
                .update(getContext(1))(room)(test._1).getOrElse(fail("Undefined Model"))

              val outcome: Outcome[MyFiringModel] = updatedModel.update(getContext(0.5))(room)(test._1)
              assert(outcome.globalEventsOrNil == List(ShotEvent(model.boundingBox.center.toVector2, test._2)))
            }
          }
        }
      }
    )
  }
}
