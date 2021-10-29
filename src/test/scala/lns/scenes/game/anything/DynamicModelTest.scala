package lns.scenes.game.anything

import indigo.shared.FrameContext
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.core.ContextFixture
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.{ BeforeAndAfterEach, Suite }

case class MyDynamicModel(
    boundingBox: BoundingBox,
    nextSpeed: Vector2,
    speed: Vector2 = Vector2(0, 0)
) extends DynamicModel {
  type Model = MyDynamicModel

  override def withDynamic(boundingBox: BoundingBox, speed: Vector2): MyDynamicModel = copyMacro

  def computeSpeed(context: FrameContext[StartupData]) = nextSpeed
}

trait DynamicModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  var model: MyDynamicModel       = _
  var movingModel: MyDynamicModel = _

  override def beforeEach() = {
    model = new MyDynamicModel(BoundingBox(centerWidth, centerHeight, 10, 10), Vector2(0, 0))
    movingModel = new MyDynamicModel(BoundingBox(centerWidth, centerHeight, 10, 10), Vector2(2, 2))

    super.beforeEach()
  }
}

class DynamicModelTest extends AnyFreeSpec with DynamicModelFixture {

  "A DynamicModel placed in room center" - {
    "after one frame update with" - {
      "time delta 1s having" - {
        "null speed should dont move" in {
          val updatedModel = model
            .update(getContext(1))(room)
            .unsafeGet

          assert(updatedModel.boundingBox.x == centerWidth && updatedModel.boundingBox.y == centerHeight)
        }
        "costant speed (2,2) should move by 2,2" in {
          val updatedModel = movingModel
            .update(getContext(1))(room)
            .unsafeGet

          assert(updatedModel.boundingBox.x == centerWidth + 2 && updatedModel.boundingBox.y == centerHeight + 2)
        }
      }
      "time delta 2s having" - {
        "null speed should dont move" in {
          val updatedModel = model
            .update(getContext(2))(room)
            .unsafeGet

          assert(updatedModel.boundingBox.x == centerWidth && updatedModel.boundingBox.y == centerHeight)
        }
        "costant speed (2,2) should move by (4,4)" in {
          val updatedModel = movingModel
            .update(getContext(2))(room)
            .unsafeGet

          assert(updatedModel.boundingBox.x == centerWidth + 4 && updatedModel.boundingBox.y == centerHeight + 4)
        }
      }
    }
    "after two frame updates with" - {
      "time delta 1s having" - {
        "null speed should dont move" in {
          val updatedModel: MyDynamicModel = model
            .update(getContext(1))(room)
            .unsafeGet

          val updatedModel2 = updatedModel.update(getContext(1))(room).unsafeGet

          assert(updatedModel2.boundingBox.x == centerWidth && updatedModel2.boundingBox.y == centerHeight)
        }
        "costant speed (2,2) should move by (4,4)" in {
          val updatedModel: MyDynamicModel = movingModel
            .update(getContext(1))(room)
            .unsafeGet

          val updatedModel2 = updatedModel.update(getContext(1))(room).unsafeGet

          assert(updatedModel2.boundingBox.x == centerWidth + 4 && updatedModel2.boundingBox.y == centerHeight + 4)
        }
      }
    }
  }
}
