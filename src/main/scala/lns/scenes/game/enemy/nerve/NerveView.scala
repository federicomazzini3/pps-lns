package lns.scenes.game.enemy.nerve

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingView, AnythingViewModel, SimpleAnythingView }
import lns.core.Assets.Enemies.Nerve.*

/**
 * Base view for a [[NerveModel]]
 * @tparam VM
 *   the [[AnythingViewModel]] required to draw elements on screen or [[Unit]] if the view works without a viewModel
 */
trait NerveView[VM <: AnythingViewModel[NerveModel] | Unit] extends AnythingView[NerveModel, VM] {}

/**
 * Nerve view. Designed for a [[NerveModel]] using no viewModel data. Built grouping its elements body and shadow
 */
object NerveView extends NerveView[Unit] with SimpleAnythingView with Nerve {

  type View = Group

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    Group()
      //.addChild(boundingModel)
      .addChild(
        Group()
          .withRef(0, offsetY)
          .addChild(bodyView(model))
      )
      .withScale(Vector2(5, 5))
}
