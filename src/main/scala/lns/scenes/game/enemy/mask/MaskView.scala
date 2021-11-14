package lns.scenes.game.enemy.mask

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingView, AnythingViewModel, SimpleAnythingView }
import lns.core.Assets.Enemies.Mask.*

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
    Group()
      //.addChild(boundingModel)
      .addChild(
        Group()
          .withRef(0, offsetY)
          .addChild(shadowModel)
          .addChild(headView(model))
      )
      .withScale(Vector2(5, 5))
}
