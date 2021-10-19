package lns.scenes.game.anything

import indigo.platform.assets.DynamicText
import indigo.shared.{ AnimationsRegister, BoundaryLocator, FontRegister, FrameContext }
import indigo.shared.datatypes.{ Rectangle, Vector2 }
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.time.{ GameTime, Seconds }
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomModel
import org.junit.jupiter.api.Assertions.assertFalse
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

trait DynamicModelFixture extends BeforeAndAfterEach { this: Suite =>

  val width           = 1600
  val height          = 900
  val centerWidth     = width / 2
  val centerHeight    = height / 2
  val startupData     = StartupData(screenDimensions = Rectangle(0, 0, width, height))
  val inputState      = InputState.default
  val dice            = Dice.fromSeed(1000)
  val boundaryLocator = new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText)
  val room            = RoomModel.initial(startupData)

  val context1 = new FrameContext[StartupData](
    GameTime.withDelta(Seconds(10), Seconds(1)),
    dice,
    inputState,
    boundaryLocator,
    startupData
  )

  val context2 = new FrameContext[StartupData](
    GameTime.withDelta(Seconds(10), Seconds(2)),
    dice,
    inputState,
    boundaryLocator,
    startupData
  )

  var model: MyDynamicModel       = _
  var movingModel: MyDynamicModel = _

  override def beforeEach() = {
    model = new MyDynamicModel(BoundingBox(centerWidth, centerHeight, 10, 10), Vector2(0, 0))
    movingModel = new MyDynamicModel(BoundingBox(centerWidth, centerHeight, 10, 10), Vector2(0, 0))

    super.beforeEach()
  }
}

class DynamicModelTest extends AnyFreeSpec with DynamicModelFixture {

  "A DynamicModel placed in room center" - {
    "after one frame update with" - {
      "time delta 1s having" - {
        "null speed should dont move" in {
          val updatedModel = model
            .update(context1)(room)
            .unsafeGet

          assert(updatedModel.boundingBox.x == centerWidth && updatedModel.boundingBox.y == centerHeight)
        }
        "costant speed (2,2) should move by 2,2" in {
          val updatedModel = movingModel
            .update(context1)(room)
            .unsafeGet

          assert(updatedModel.boundingBox.x == centerWidth + 2 && updatedModel.boundingBox.y == centerHeight + 2)
        }
      }
      "time delta 2s having" - {
        "null speed should dont move" in {
          val updatedModel = model
            .update(context2)(room)
            .unsafeGet

          assert(updatedModel.boundingBox.x == centerWidth && updatedModel.boundingBox.y == centerHeight)
        }
        "costant speed (2,2) should move by (4,4)" in {
          val updatedModel = movingModel
            .update(context2)(room)
            .unsafeGet

          assert(updatedModel.boundingBox.x == centerWidth + 4 && updatedModel.boundingBox.y == centerHeight + 4)
        }
      }
    }
    "after two frame updates with" - {
      "time delta 1s having" - {
        "null speed should dont move" in {
          val updatedModel: MyDynamicModel = model
            .update(context1)(room)
            .unsafeGet

          val updatedModel2 = updatedModel.update(context1)(room).unsafeGet

          assert(updatedModel2.boundingBox.x == centerWidth && updatedModel2.boundingBox.y == centerHeight)
        }
        "costant speed (2,2) should move by (4,4)" in {
          val updatedModel: MyDynamicModel = movingModel
            .update(context1)(room)
            .unsafeGet

          val updatedModel2 = updatedModel.update(context1)(room).unsafeGet

          assert(updatedModel2.boundingBox.x == centerWidth + 4 && updatedModel2.boundingBox.y == centerHeight + 4)
        }
      }
    }
  }
}
