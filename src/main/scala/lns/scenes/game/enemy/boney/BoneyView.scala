package lns.scenes.game.enemy.boney

import indigo.*
import indigo.shared.datatypes.Vector2
import lns.StartupData
import lns.scenes.game.anything.AnythingView
import lns.core.Assets.Enemies.Boney.*

/**
 * Boney view based on EnemyModel and built grouping its elements head, body and shadow
 */
object BoneyView extends AnythingView with Boney {

  type Model     = BoneyModel
  type ViewModel = Unit
  type View      = Group

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
