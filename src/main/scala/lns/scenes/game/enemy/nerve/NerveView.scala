package lns.scenes.game.enemy.nerve

import indigo.*
import indigo.shared.datatypes.Vector2
import lns.StartupData
import lns.scenes.game.anything.AnythingView

/**
 * Nerve view based on EnemyModel and built grouping its elements head and shadow
 */
case class NerveView() extends AnythingView with Nerve {

  type Model     = NerveModel
  type ViewModel = Unit
  type View      = Group

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    Group()
      /* .addChild(boundingModel) */
      .addChild(bodyView(model))
      .withScale(Vector2(5, 5))
  //.withRef(width / 2, height / 2)
  //.withScale(Vector2(scale, scale))
}
