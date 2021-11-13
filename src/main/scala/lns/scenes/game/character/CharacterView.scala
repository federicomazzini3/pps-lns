package lns.scenes.game.character

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingId, AnythingView, AnythingViewModel }
import lns.core.Assets.Character.*

trait CharacterView[VM <: AnythingViewModel[CharacterModel] | Unit] extends AnythingView[CharacterModel, VM] {}

/**
 * Character Isaac view based on CharacterModel and built grouping its elements head, body and shadow
 */
object CharacterView extends CharacterView[CharacterViewModel] with Isaac {

  type View = Group

  def viewModel: (id: AnythingId) => ViewModel = CharacterViewModel.initial

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    Group()
      //.addChild(boundingModel)
      .addChild(
        Group()
          .withRef(0, offsetY)
          .addChild(shadowModel)
          .addChild(bodyView(model))
          .addChild(headView(model, viewModel))
      )
      .withScale(Vector2(5, 5))
}
