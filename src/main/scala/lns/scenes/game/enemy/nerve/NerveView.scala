package lns.scenes.game.enemy.nerve

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingView, AnythingViewModel, SimpleAnythingView }
import lns.core.Assets.Enemies.Nerve.*

trait NerveView[VM <: AnythingViewModel[NerveModel] | Unit] extends AnythingView[NerveModel, VM] {}

/**
 * Nerve view based on EnemyModel and built grouping its elements head and shadow
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
