package lns.scenes.game.enemy.parabite

import indigo.*
import indigo.shared.datatypes.Vector2
import lns.StartupData
import lns.scenes.game.anything.AnythingView

/**
 * Parabite view based on EnemyModel and built grouping its elements head, body and shadow
 */
case class ParabiteView() extends AnythingView with Parabite {

  type Model     = ParabiteModel
  type ViewModel = ParabiteViewModel
  type View      = Group

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    Group()
      /* .addChild(boundingModel) */
      .addChild(bodyView(model, viewModel))
      .withScale(Vector2(5, 5))
}
