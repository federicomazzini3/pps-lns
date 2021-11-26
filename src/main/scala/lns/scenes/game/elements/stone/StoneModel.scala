package lns.scenes.game.elements.stone

import indigoextras.geometry.BoundingBox
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.*
import lns.scenes.game.elements.ElementModel

case class StoneModel(
    id: AnythingId,
    view: () => StoneView[_],
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    crossable: Boolean = false
) extends ElementModel {

  type Model = StoneModel

  def withSolid(crossable: Boolean): Model = copyMacro

}
