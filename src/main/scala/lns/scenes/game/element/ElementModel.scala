package lns.scenes.game.element

import indigoextras.geometry.{ BoundingBox, Vertex }
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
    Map(
      UUID.randomUUID() -> StoneModel(
        BoundingBox(
          Vertex(470, 300),
          Vertex(Stone.width, Stone.height - Stone.offsetY)
        )
      ),
      UUID.randomUUID() -> StoneModel(
        BoundingBox(
          Vertex(470 + Stone.width, 300),
          Vertex(Stone.width, Stone.height - Stone.offsetY)
        )
      ),
      UUID.randomUUID() -> StoneModel(
        BoundingBox(
          Vertex(470 + Stone.width * 2, 300),
          Vertex(Stone.width, Stone.height - Stone.offsetY)
        )
      ),
      UUID.randomUUID() -> StoneModel(
        BoundingBox(
          Vertex(470 + Stone.width * 2, 300 + Stone.height - Stone.offsetY),
          Vertex(Stone.width, Stone.height - Stone.offsetY)
        )
      )
    )
}
