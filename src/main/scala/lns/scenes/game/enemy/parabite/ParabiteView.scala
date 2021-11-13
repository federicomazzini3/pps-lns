package lns.scenes.game.enemy.parabite

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingId, AnythingView, AnythingViewModel }

trait ParabiteView[VM <: AnythingViewModel[ParabiteModel] | Unit] extends AnythingView[ParabiteModel, VM] {}

/**
 * Parabite view based on EnemyModel and built grouping its elements head, body and shadow
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
