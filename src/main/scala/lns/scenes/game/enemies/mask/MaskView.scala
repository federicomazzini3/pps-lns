package lns.scenes.game.enemies.mask

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingView, AnythingViewModel, SimpleAnythingView }

/**
 * Base view for a [[MaskModel]]
 * @tparam VM
 *   the [[AnythingViewModel]] required to draw elements on screen or [[Unit]] if the view works without a viewModel
 */
trait MaskView[VM <: AnythingViewModel[MaskModel] | Unit] extends AnythingView[MaskModel, VM] {}

/**
 * Boney view. Designed for a [[MaskModel]] using no viewModel data. Built grouping its elements head and shadow
 */
object MaskView extends MaskView[Unit] with SimpleAnythingView with Mask {

  type View = Group

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    drawComponents(List(shadowView, headView(model)))
}
