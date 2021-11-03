package lns.scenes.game.enemy

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

case class MyFollowerModel(
    boundingBox: BoundingBox,
    maxSpeed: Int,
    speed: Vector2 = Vector2(0, 0)
) extends DynamicModel
    with Follower(maxSpeed) {
  type Model = MyFollowerModel

  override def withDynamic(boundingBox: BoundingBox, speed: Vector2): MyFollowerModel = copyMacro
}

trait FollowerModelFixture extends ContextFixture with BeforeAndAfterEach { this: Suite =>

  var model: MyFollowerModel             = _
  val maxSpeed                           = 2
  val initialPos                         = 100
  override val character: CharacterModel = CharacterModel.initial.withDynamic(BoundingBox(0, 0, 10, 10), Vector2(0, 0))

  override def beforeEach() = {
    model = new MyFollowerModel(BoundingBox(initialPos, initialPos, 10, 10), maxSpeed)

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
          "move by $maxSpeed" in {
            val updatedModel: MyFollowerModel = model
              .update(getContext(1))(room)(character)
              .getOrElse(fail("Undefined Model"))

            val distance = updatedModel.getPosition().distanceTo(Vector2(initialPos, initialPos))
            assert(distance < maxSpeed + 0.001 && distance > maxSpeed - 0.001)
          }
          s"move in direction (-1,-1)" in {
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
