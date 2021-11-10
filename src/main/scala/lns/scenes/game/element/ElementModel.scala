package lns.scenes.game.element

import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.core.Assets.Rooms
import lns.scenes.game.anything.{ AnythingModel, SolidModel }

import java.util.UUID

case class StoneModel(val boundingBox: BoundingBox) extends SolidModel {

  type Model = StoneModel

  val enabled = true
}

object ElementModel {
  import lns.core.Assets.Elements.*

  /*def stone(position: Vertex): StoneModel =
    StoneModel(
      BoundingBox(
        position,
        Vertex(Stone.withScale(Stone.width), Stone.withScale(Stone.height))
      )
    )*/

  def stone(): Map[UUID, AnythingModel] =
    val stones =
      for {
        i <- 0 until 9 if i != 4
        j <- 0 until 2
      } yield UUID.randomUUID() -> StoneModel(
        BoundingBox(
          Vertex(Rooms.cellSize * i, Rooms.cellSize * j),
          Vertex(Stone.withScale(Stone.width), Stone.withScale(Stone.height - Stone.offsetY))
        )
      )

    stones.toMap
}
