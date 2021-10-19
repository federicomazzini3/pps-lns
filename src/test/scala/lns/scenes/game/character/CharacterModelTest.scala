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
}
