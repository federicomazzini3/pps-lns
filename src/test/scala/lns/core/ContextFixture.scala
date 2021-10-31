package lns.core

import indigo.platform.assets.DynamicText
import indigo.shared.datatypes.Rectangle
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.time.{ GameTime, Seconds }
import indigo.shared.{ AnimationsRegister, BoundaryLocator, FontRegister, FrameContext }
import lns.StartupData
import lns.scenes.game.room.RoomModel
import org.scalatest.Suite

trait ContextFixture {
  this: Suite =>

  val width           = 1920
  val height          = 1080
  val centerWidth     = width / 2
  val centerHeight    = height / 2
  val startupData     = StartupData(screenDimensions = Rectangle(0, 0, width, height))
  val dice            = Dice.fromSeed(1000)
  val boundaryLocator = new BoundaryLocator(new AnimationsRegister, new FontRegister, new DynamicText)
  val room            = RoomModel.initial()

  def getContext(timeDelta: Double, inputState: InputState = InputState.default): FrameContext[StartupData] =
    new FrameContext[StartupData](
      GameTime.withDelta(Seconds(10 + timeDelta), Seconds(timeDelta)),
      dice,
      inputState,
      boundaryLocator,
      startupData
    )

  def getSubSytemContext(timeDelta: Double, inputState: InputState = InputState.default): SubSystemFrameContext =
    new SubSystemFrameContext(
      GameTime.withDelta(Seconds(10 + timeDelta), Seconds(timeDelta)),
      dice,
      inputState,
      boundaryLocator
    )
}
