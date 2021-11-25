package lns.scenes.game.bosses

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingId, AnythingView, AnythingViewModel }

/**
 * Base view for a [[BossModel]]
 * @tparam VM
 *   the [[AnythingViewModel]] required to draw elements on screen or [[Unit]] if the view works without a viewModel
 */
trait BossView[VM <: AnythingViewModel[BossModel] | Unit] extends AnythingView[BossModel, VM] {}

/**
 * Boos view. Designed for a [[BossModel]] using a [[BossModelModel]] for the view data. Built grouping its elements
 * body and shadow
 */
object BossView extends BossView[BossViewModel] with Loki {

  type View = Group

  def viewModel: (id: AnythingId) => ViewModel = BossViewModel.initial

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    drawComponents(List(shadowView, bodyView(model, viewModel)))
}
