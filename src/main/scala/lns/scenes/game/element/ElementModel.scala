package lns.scenes.game.element

import scala.language.implicitConversions

import indigo.*
import indigoextras.geometry.BoundingBox
import lns.core.Assets.Rooms
import lns.scenes.game.anything.{ AnythingId, AnythingModel, SolidModel, given }

import java.util.UUID

case class StoneModel(val id: AnythingId, val boundingBox: BoundingBox, val shotAreaOffset: Int) extends SolidModel {

  type Model = StoneModel

  val crossable = false
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

  def defaultArea(i: Int, j: Int): BoundingBox = BoundingBox(
    Vector2(Rooms.cellSize * i, Rooms.cellSize * j),
    Vector2(Stone.withScale(Stone.width), Stone.withScale(Stone.height - Stone.offsetY))
  )

  def stone(): Map[AnythingId, AnythingModel] =
    val stones =
      for {
        i <- 0 until 9 if i != 4
        j <- 0 until 2
        id = AnythingId.generate
      } yield id -> StoneModel(
        id,
        BoundingBox(
          Vector2(Rooms.cellSize * i, Rooms.cellSize * j),
          Vector2(Stone.withScale(Stone.width), Stone.withScale(Stone.height - Stone.offsetY))
        ),
        shotAreaOffset = 0
      )

    stones.toMap
}
