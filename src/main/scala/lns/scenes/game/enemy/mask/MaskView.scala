package lns.scenes.game.enemy.mask

import indigo.*
import indigo.shared.datatypes.Vector2
import lns.StartupData
import lns.scenes.game.anything.AnythingView
import lns.core.Assets.Enemies.Mask.*

/**
 * Boney view based on EnemyModel and built grouping its elements head, body and shadow
 */
case class MaskView() extends AnythingView with Mask {

  type Model     = MaskModel
  type ViewModel = Unit
  type View      = Group

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
