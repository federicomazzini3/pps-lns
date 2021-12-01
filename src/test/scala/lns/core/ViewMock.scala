package lns.core

import indigo.*
import indigo.shared.FrameContext
import lns.StartupData
import lns.scenes.game.anything.{ AnythingModel, AnythingView, SimpleAnythingView }
import scala.annotation.nowarn

/**
 * View mock not bounded to a specific [[AnythingModel]]
 * @tparam M
 *   the [[AnythingModel]] for which the View mock is created in a test
 */
@nowarn
class ViewMock[M <: AnythingModel] extends AnythingView[M, Unit] with SimpleAnythingView {
  type View = Group

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View = Group()
}
