package lns.scenes.game.anything

import indigo.*
import indigo.platform.assets.*
import indigo.shared.*
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.input.{ Gamepad, Keyboard, Mouse }
import indigo.shared.scenegraph.*
import indigo.shared.time.GameTime
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Assets
import lns.scenes.game.*
import lns.scenes.menu.MenuScene

import scala.annotation.targetName
import scala.reflect.*
import scala.language.implicitConversions

given Conversion[Vector2, Point] with
  def apply(v: Vector2): Point = Point(v.x.toInt, v.y.toInt)

/**
 * Base view for every thing placed inside a room
 */
trait AnythingView[M <: AnythingModel: Typeable, VM <: AnythingViewModel[M] | Unit: Typeable] {
  type Model     = M
  type ViewModel = VM
  type View <: Group

  def viewModel: (id: AnythingId) => ViewModel

  /**
   * Build the view object to be drawn based on model and viewModel data
   * @param contex
   *   indigo frame context data
   * @param model
   *   the model of the Anything to be drawn
   * @param viewModel
   *   the viewModel of the Anything to be drawn
   * @return
   *   the view object to be drawn
   */
  protected def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View

  protected def depth(model: Model): Depth = Depth(-model.boundingBox.top.toInt)

  /**
   * Draw request called during game loop on every frame
   * @param contex
   *   indigo frame context data
   * @param model
   *   the model of the Anything to be drawn
   * @param viewModel
   *   the viewModel of the Anything to be drawn
   * @return
   *   a SceneUpdateFragment with the view of the Anything to be drawn placed at its current position
   */
  def draw(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): Group =
    view(contex, model, viewModel)
      .moveTo(model.getPosition())
      .moveBy(Assets.Rooms.wallSize, Assets.Rooms.wallSize)
      .withDepth(depth(model))

  @targetName("anyDraw")
  def draw(contex: FrameContext[StartupData], model: AnythingModel, viewModel: AnythingViewModel[_] | Unit): Group =
    (model, viewModel) match {
      case (m: Model, vm: ViewModel) => println("OKKK"); draw(contex, m, vm)
      case _                         => println("NOOO"); Group()
    }
}

trait SimpleAnythingView {
  this: AnythingView[_, Unit] =>
  def viewModel: (id: AnythingId) => ViewModel = _ => ()
}
