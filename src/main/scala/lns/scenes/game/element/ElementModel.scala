package lns.scenes.game.element

import scala.language.implicitConversions

import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.core.Assets.Rooms
import lns.scenes.game.anything.{ AnythingId, AnythingModel, SolidModel, given }
import lns.core.Macros.copyMacro

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
  import lns.core.AnythingAssets.*

  def defaultArea(i: Int, j: Int): BoundingBox =
    stone.boundingBox(Vertex(Rooms.cellSize * i, Rooms.cellSize * j))

  def stones(): Map[AnythingId, AnythingModel] =
    val stones =
      for {
        i <- 0 until 9 if i != 4
        j <- 0 until 2
        id = AnythingId.generate
      } yield id -> StoneModel(
        id = id,
        view = () => StoneView,
        boundingBox = stone.boundingBox(Vertex(Rooms.cellSize * i, Rooms.cellSize * j)),
        shotAreaOffset = 0
      )

    stones.toMap
}
