package lns.scenes.game.element

import indigo.*
import indigo.shared.FrameContext
import indigo.shared.datatypes.{ Rectangle, Vector2 }
import indigo.shared.materials.Material
import indigo.shared.scenegraph.{ Graphic, Group }
import lns.StartupData
import lns.core.Assets
import lns.scenes.game.anything.{ AnythingView, SolidModel }
import lns.scenes.game.character.{ CharacterModel, CharacterViewModel }

case class ElementView() extends AnythingView {

  type Model     = SolidModel
  type ViewModel = Unit
  type View      = Group

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    Group()
      .addChild(
        model match {
          case StoneModel(_) =>
            ElementGraphic
              .stone()
              .withRef(0, Assets.Elements.Stone.offsetY)
        }
      )
}

case object ElementGraphic {

  import Assets.Elements.Stone.*
  def stone(): Graphic[Material.Bitmap] = Graphic(
    Rectangle(
      0,
      0,
      width,
      height
    ),
    1,
    Material.Bitmap(name)
  )
}
