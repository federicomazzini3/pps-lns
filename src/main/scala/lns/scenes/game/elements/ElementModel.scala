package lns.scenes.game.elements

import scala.language.implicitConversions
import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.core.Assets.Rooms
import lns.scenes.game.anything.{ AnythingId, AnythingModel, SolidModel }
import lns.core.Macros.copyMacro
import lns.scenes.game.room.Cell

case class StoneModel(
    id: AnythingId,
    view: () => StoneView[_],
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    crossable: Boolean = false
) extends SolidModel {

  type Model = StoneModel

  def withSolid(crossable: Boolean): Model = copyMacro

}

object ElementModel {

  def stones(cells: Seq[Cell]): Map[AnythingId, AnythingModel] =
    def _stones(cells: Seq[Cell], stones: Map[AnythingId, AnythingModel]): Map[AnythingId, AnythingModel] =
      if cells.size <= 0 then stones
      else {
        val stone = StoneModel(
          id = AnythingId.generate,
          view = () => StoneView,
          boundingBox = StoneView.boundingBox(Vertex(Rooms.cellSize * cells.head.x, Rooms.cellSize * cells.head.y)),
          shotAreaOffset = 0
        )
        _stones(cells.tail, stones + (stone.id -> stone))
      }
    _stones(cells, Map.empty)
}
