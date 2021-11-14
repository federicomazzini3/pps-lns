package lns.scenes.game.anything

import indigo.*
import indigo.platform.assets.*
import indigo.shared.*
import indigo.shared.dice.Dice
import indigo.shared.events.InputState
import indigo.shared.input.{ Gamepad, Keyboard, Mouse }
import indigo.shared.scenegraph.*
import indigo.shared.time.GameTime
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
 * Base view for every Anything placed inside a room. A view is binded and designed for a specific [[AnythingModel]] and
 * may require an [[AnythingViewModel]] to represent the Model to screen
 * @tparam M
 *   the [[AnythingModel]] for which the View is designed
 * @tparam VM
 *   the [[AnythingViewModel]] required to draw elements on screen or [[Unit]] if the view works without a viewModel
 */
trait AnythingView[M <: AnythingModel: Typeable, VM <: AnythingViewModel[M] | Unit: Typeable] {
  type Model     = M
  type ViewModel = VM
  type View <: Group

  /**
   * viewModel factory
   * @return
   *   the ViewModel that maybe an [[AnythingViewModel]] or [[Unit]] if a viewModel not required by the view
   */
  def viewModel: (id: AnythingId) => ViewModel

  /**
   * Builds the view object to be drawn based on model and viewModel data
   * @param contex
   *   Indigo frame context data
   * @param model
   *   the model of the Anything to be drawn
   * @param viewModel
   *   the viewModel of the Anything to be drawn
   * @return
   *   the view object to be drawn
   */
  protected def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View

  /**
   * Computes the view depth layer for an Anything based on current vertical position
   * @param model
   *   [[AnythingModel]] data
   * @return
   *   Indigo Depth object
   */
  protected def depth(model: Model): Depth = Depth(-model.boundingBox.top.toInt)

  /**
   * Draw request called during game loop on every frame
   * @param contex
   *   Indigo frame context data
   * @param model
   *   the model of the Anything to be drawn. Needs to be of type Model for which the View is designed
   * @param viewModel
   *   the viewModel of the Anything to be drawn. Needs to be of type ViewModel that the View requires for the Model
   * @return
   *   a Group with the view of the Anything to be drawn placed at its current position
   */
  def draw(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): Group =
    view(contex, model, viewModel)
      .moveTo(model.getPosition())
      .moveBy(Assets.Rooms.wallSize, Assets.Rooms.wallSize)
      .withDepth(depth(model))

  /**
   * Generic Draw that accepts every [[AnythingModel]] or [[AnythingViewModel]] and checks for type correctness at
   * runtime. Useful when you need to draw an [[AnythingModel]] you don't know its type. If the type is incorrect then
   * draws an empty Group
   * @param contex
   *   Indigo frame context data
   * @param model
   *   the model of the Anything to be drawn.
   * @param viewModel
   *   the viewModel of the Anything to be drawn.
   * @return
   *   a Group with the view of the Anything to be drawn placed at its current position. Or and empty Group if type
   *   check fails
   */
  @targetName("anyDraw")
  def draw(contex: FrameContext[StartupData], model: AnythingModel, viewModel: AnythingViewModel[_] | Unit): Group =
    (model, viewModel) match {
      case (m: Model, vm: ViewModel) => draw(contex, m, vm)
      case _                         => Group()
    }
}

/**
 * Base view that requires no [[AnythingViewModel]]
 */
trait SimpleAnythingView {
  this: AnythingView[_, Unit] =>
  def viewModel: (id: AnythingId) => ViewModel = _ => ()
}
