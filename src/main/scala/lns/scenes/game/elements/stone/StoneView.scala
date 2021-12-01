package lns.scenes.game.elements.stone

import indigo.*
import indigo.shared.FrameContext
import indigo.shared.materials.Material
import indigo.shared.scenegraph.{ Graphic, Group }
import lns.StartupData
import lns.core.Assets
import lns.scenes.game.anything.{ AnythingView, AnythingViewModel, SimpleAnythingView, SolidModel }
import lns.scenes.game.characters.{ CharacterModel, CharacterViewModel }
import lns.scenes.game.elements.stone.{ Stone, StoneModel }

/**
 * Base view for a [[StoneModel]]
 * @tparam VM
 *   the [[AnythingViewModel]] required to draw elements on screen or [[Unit]] if the view works without a viewModel
 */
trait StoneView[VM <: AnythingViewModel[StoneModel] | Unit] extends AnythingView[StoneModel, VM] {}

/**
 * Stone view. Designed for a [[StoneModel]] using no viewModel data. Built grouping its elements head, body and shadow
 */
object StoneView extends StoneView[Unit] with SimpleAnythingView with Stone {

  type View = Group

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    drawComponents(List(stoneView))
}
