package lns.scenes.game.shot

import scala.language.implicitConversions

import indigo.shared.{ FrameContext, Outcome }
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.ContextFixture
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.AnythingId
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }

trait ShotModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  var model: ShotModel = _

  val stats = Stats(
    FireDamage -> 3.5,
    FireRange  -> 200,
    FireRate   -> 2,
    FireSpeed  -> 100
  )

  val fireDirection = Vector2(1, 0);

  def createShot(direction: Vector2): ShotModel =
    ShotModel(AnythingId.generate, Vector2(roomCenterX, roomCenterY), direction, Stats.createShot(stats))

}
class ShotModelTest extends AnyFreeSpec with ShotModelFixture {
  s"A ShotModel with range ${FireRange @@ stats} and speed ${FireSpeed @@ stats}" - {
    List(Vector2(1, 0), Vector2(-1, 0), Vector2(0, 1), Vector2(0, -1)).foreach { direction =>
      s" shot in the direction $direction" - {
        "after 1 second should decrease the range of his speed" in {
          val model = createShot(direction)
          val updatedModel =
            model.update(getContext(1))(gameContext).getOrElse(fail("Undefined Model"))

          assert(updatedModel.life == 1)
          assert(updatedModel.range == FireRange @@ stats - FireSpeed @@ stats)
        }
        "after 2 second the rage should be zero" in {
          val model = createShot(direction)
          val updatedModel =
            model.update(getContext(2))(gameContext).getOrElse(fail("Undefined Model"))

          assert(updatedModel.life == 1)
          assert(updatedModel.range == 0)
        }
        "when range is zero his life should be zero next frame" in {
          val model = createShot(direction)
          val updatedModel = model
            .update(getContext(2))(gameContext)
            .getOrElse(fail("Undefined Model"))
            .update(getContext(0.016))(gameContext)
            .getOrElse(fail("Undefined Model"))

          assert(updatedModel.life == 0)
          assert(updatedModel.range == 0)
        }
      }
    }
  }
}
