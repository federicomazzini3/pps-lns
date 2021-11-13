package lns.scenes.game.anything

import scala.language.implicitConversions
import indigo.shared.FrameContext
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.core.{ ContextFixture, ViewMock }
import lns.scenes.game.GameContext
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }

case class MyDynamicModel(
    id: AnythingId,
    view: () => ViewMock[MyDynamicModel],
    boundingBox: BoundingBox,
    stats: Stats,
    nextSpeed: Vector2,
    speed: Vector2 = Vector2(0, 0)
) extends DynamicModel {
  type Model = MyDynamicModel

  def withDynamic(boundingBox: BoundingBox, speed: Vector2): MyDynamicModel = copyMacro
  def withStats(stats: Stats): Model                                        = copyMacro

  def computeSpeed(context: FrameContext[StartupData])(gameContext: GameContext) = nextSpeed
}

trait DynamicModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  var model: MyDynamicModel       = _
  var movingModel: MyDynamicModel = _

  val stats = Stats(MaxSpeed -> 300)

  override def beforeEach() = {
    model = new MyDynamicModel(
      AnythingId.generate,
      () => new ViewMock[MyDynamicModel],
      BoundingBox(roomCenterX, roomCenterY, 10, 10),
      stats,
      Vector2(0, 0)
    )
    movingModel = new MyDynamicModel(
      AnythingId.generate,
      () => new ViewMock[MyDynamicModel],
      BoundingBox(roomCenterX, roomCenterY, 10, 10),
      stats,
      Vector2(2, 2)
    )

    super.beforeEach()
  }
}

class DynamicModelTest extends AnyFreeSpec with DynamicModelFixture {

  "A DynamicModel placed in room center" - {
    "after one frame update with" - {
      "time delta 1s having" - {
        "null speed should dont move" in {
          val updatedModel = model
            .update(getContext(1))(gameContext)
            .getOrElse(fail("Undefined Model"))

          assert(updatedModel.boundingBox.x == roomCenterX && updatedModel.boundingBox.y == roomCenterY)
          assert(updatedModel.isMoving() == false)
        }
        "costant speed (2,2) should move by 2,2" in {
          val updatedModel = movingModel
            .update(getContext(1))(gameContext)
            .getOrElse(fail("Undefined Model"))

          assert(updatedModel.boundingBox.x == roomCenterX + 2 && updatedModel.boundingBox.y == roomCenterY + 2)
          assert(updatedModel.isMoving() == true)
        }
      }
      "time delta 2s having" - {
        "null speed should dont move" in {
          val updatedModel = model
            .update(getContext(2))(gameContext)
            .getOrElse(fail("Undefined Model"))

          assert(updatedModel.boundingBox.x == roomCenterX && updatedModel.boundingBox.y == roomCenterY)
          assert(updatedModel.isMoving() == false)
        }
        "costant speed (2,2) should move by (4,4)" in {
          val updatedModel = movingModel
            .update(getContext(2))(gameContext)
            .getOrElse(fail("Undefined Model"))

          assert(updatedModel.boundingBox.x == roomCenterX + 4 && updatedModel.boundingBox.y == roomCenterY + 4)
          assert(updatedModel.isMoving() == true)
        }
      }
    }
    "after two frame updates with" - {
      "time delta 1s having" - {
        "null speed should dont move" in {
          val updatedModel: MyDynamicModel = model
            .update(getContext(1))(gameContext)
            .getOrElse(fail("Undefined Model"))

          val updatedModel2 = updatedModel.update(getContext(1))(gameContext).unsafeGet

          assert(updatedModel2.boundingBox.x == roomCenterX && updatedModel2.boundingBox.y == roomCenterY)
          assert(updatedModel.isMoving() == false)
        }
        "costant speed (2,2) should move by (4,4)" in {
          val updatedModel: MyDynamicModel = movingModel
            .update(getContext(1))(gameContext)
            .getOrElse(fail("Undefined Model"))

          val updatedModel2 = updatedModel.update(getContext(1))(gameContext).unsafeGet

          assert(updatedModel2.boundingBox.x == roomCenterX + 4 && updatedModel2.boundingBox.y == roomCenterY + 4)
          assert(updatedModel.isMoving() == true)
        }
      }
    }
  }
}
