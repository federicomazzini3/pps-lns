package lns.scenes.game.element

import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.scenes.game.anything.{ AnythingModel, SolidModel }

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

  def stone(): Set[AnythingModel] =
    Set(
      StoneModel(
        BoundingBox(
          Vertex(470, 300),
          Vertex(Stone.width, Stone.height)
        )
      ),
      StoneModel(
        BoundingBox(
          Vertex(470 + Stone.width, 300),
          Vertex(Stone.width, Stone.height)
        )
      ),
      StoneModel(
        BoundingBox(
          Vertex(470 + Stone.width * 2, 300),
          Vertex(Stone.width, Stone.height)
        )
      ),
      StoneModel(
        BoundingBox(
          Vertex(470 + Stone.width * 2, 300 + Stone.height),
          Vertex(Stone.width, Stone.height)
        )
      )
    )
}
