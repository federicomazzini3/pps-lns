package lns.scenes.game.character

import indigo.*
import indigo.shared.datatypes.Vector2
import lns.StartupData
import lns.scenes.game.anything.AnythingView
import lns.core.Assets.Character.*

/**
 * Character Isaac view based on CharacterModel and built grouping its elements head, body and shadow
 */
case class CharacterView() extends AnythingView with Isaac {

  type Model     = CharacterModel
  type ViewModel = CharacterViewModel
  type View      = Group

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
  //.withDepth(Depth(-model.boundingBox.top.toInt))
  //.withRef(width / 2, height / 2)
  //.withScale(Vector2(scale, scale))
}
