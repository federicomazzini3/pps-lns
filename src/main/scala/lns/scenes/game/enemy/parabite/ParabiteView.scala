package lns.scenes.game.enemy.parabite

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingId, AnythingView, AnythingViewModel }

/**
 * Base view for a [[ParabiteModel]]
 * @tparam VM
 *   the [[AnythingViewModel]] required to draw elements on screen or [[Unit]] if the view works without a viewModel
 */
trait ParabiteView[VM <: AnythingViewModel[ParabiteModel] | Unit] extends AnythingView[ParabiteModel, VM] {}

/**
 * Parabite view. Designed for a [[ParabiteModel]] using a [[ParabiteViewModel]] for the view data. Built grouping its
 * elements body and shadow
 */
object ParabiteView extends ParabiteView[ParabiteViewModel] with Parabite {

  type View = Group

  def viewModel: (id: AnythingId) => ViewModel = ParabiteViewModel.initial

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    Group()
      /* .addChild(boundingModel) */
      .addChild(bodyView(model, viewModel))
      .withScale(Vector2(5, 5))
}
