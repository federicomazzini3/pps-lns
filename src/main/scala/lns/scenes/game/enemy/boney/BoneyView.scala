package lns.scenes.game.enemy.boney

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingView, AnythingViewModel, SimpleAnythingView }
import lns.core.Assets.Enemies.Boney.*

/**
 * Base view for a [[BoneyModel]]
 * @tparam VM
 *   the [[AnythingViewModel]] required to draw elements on screen or [[Unit]] if the view works without a viewModel
 */
trait BoneyView[VM <: AnythingViewModel[BoneyModel] | Unit] extends AnythingView[BoneyModel, VM] {}

/**
 * Boney view. Designed for a [[BoneyModel]] using no viewModel data. Built grouping its elements head, body and shadow
 */
object BoneyView extends BoneyView[Unit] with SimpleAnythingView with Boney {

  type View = Group

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    Group()
      //.addChild(boundingModel)
      .addChild(
        Group()
          .withRef(0, offsetY)
          .addChild(shadowModel)
          .addChild(bodyView(model))
          .addChild(headView(model))
      )
      .withScale(Vector2(5, 5))
}
