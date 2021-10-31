package lns.scenes.game.character

import indigo.*
import indigo.shared.datatypes.Vector2
import lns.StartupData
import lns.scenes.game.anything.AnythingView

/**
 * Character Isaac view based on CharacterModel and built grouping its elements head, body and shadow
 */
case class CharacterView() extends AnythingView with Isaac {

  type Model     = CharacterModel
  type ViewModel = Unit
  type View      = Group

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    Group()
      /* .addChild(boundingModel) */
      .addChild(shadowModel)
      .addChild(bodyView(model))
      .addChild(headView(model))
      .withScale(Vector2(5, 5))
  //.withRef(width / 2, height / 2)
  //.withScale(Vector2(scale, scale))
}
