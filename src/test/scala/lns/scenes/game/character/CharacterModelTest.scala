package lns.scenes.game.character

import indigoextras.geometry.Vertex
import org.junit.jupiter.api.Assertions.assertFalse
import org.scalatest.freespec.AnyFreeSpec
import lns.StartupData
import lns.scenes.game.character.*
import lns.scenes.game.room.RoomModel

class CharacterModelTest extends AnyFreeSpec {

  import indigo.*
  import indigo.shared.*
  import indigo.shared.events.KeyboardEvent.KeyDown
  import indigo.platform.assets.*

  var character = CharacterView()

  var startupData = StartupData(screenDimensions = Rectangle(0, 0, 0, 0))
  var model       = CharacterModel.initial(startupData)

  val keyboard =
    Keyboard.calculateNext(
      Keyboard.default,
      List(KeyDown(Key.LEFT_ARROW), KeyDown(Key.DOWN_ARROW))
    )

  val inputState = new InputState(Mouse.default, keyboard, Gamepad.default)

  println("OLD " + model.boundingBox)

  var newModel = model
    .update(
      new FrameContext[StartupData](
        GameTime.zero,
        Dice.fromSeed(1000),
        inputState,
        new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText),
        startupData
      )
    )
    .unsafeGet
  println("OLD NOT EDITED" + model.boundingBox)

  newModel = model
    .update(
      new FrameContext[StartupData](
        GameTime.withDelta(Seconds(1), Seconds(1.5)),
        Dice.fromSeed(1000),
        inputState,
        new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText),
        startupData
      )
    )
    .unsafeGet

  println("NEW AFTER EDIT" + newModel.boundingBox)

  "A Set" - {
    "when empty" - {
      "should have size 0" in {
        assert(Set.empty.size == 0)
      }
      "should produce NoSuchElementException when head is invoked" in {
        assertThrows[NoSuchElementException] {
          Set.empty.head
        }
      }
    }
    "when non empty" - {
      val a: Set[Int] = Set(1, 2, 3)

      "should contains something" in {
        assertFalse(a.isEmpty)
      }
    }
  }
}
