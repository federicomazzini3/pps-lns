package lns.scenes.game.anything

import indigo.*
import indigo.platform.assets.*
import indigo.shared.*
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.input.{ Gamepad, Keyboard, Mouse }
import indigo.shared.scenegraph.*
import indigo.shared.time.GameTime
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.scenes.game.*
import lns.scenes.menu.MenuScene

/*Util*/
extension (s: Group) def moveTo(vector: Vector2) = s.moveTo(vector.x.toInt, vector.y.toInt)

trait AnythingView {
  type Model <: AnythingModel
  type ViewModel <: AnythingViewModel | Unit
  type View <: Group

  protected def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View

  def draw(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): SceneUpdateFragment =
    SceneUpdateFragment(
      view(contex, model, viewModel).moveTo(model.getPosition())
    )
}
