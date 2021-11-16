package lns.scenes.game.character

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingId, AnythingView, AnythingViewModel }
import lns.core.Assets.Character.*

/**
 * Base view for a [[CharacterModel]]
 * @tparam VM
 *   the [[AnythingViewModel]] required to draw elements on screen or [[Unit]] if the view works without a viewModel
 */
trait CharacterView[VM <: AnythingViewModel[CharacterModel] | Unit] extends AnythingView[CharacterModel, VM] {}

/**
 * Character Isaac view. Designed for a [[CharacterModel]] using a [[CharacterViewModel]] for the view data. Built
 * grouping its elements head, body and shadow
 */
object CharacterView extends CharacterView[CharacterViewModel] with Isaac {

  type View = Group

  def viewModel: (id: AnythingId) => ViewModel = CharacterViewModel.initial

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    model.life match {
      case 0 => Group()
      case _ =>
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

}
