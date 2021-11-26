package lns.scenes.game.elements

import scala.language.implicitConversions
import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.core.Assets.Rooms
import lns.scenes.game.anything.{ AnythingId, AnythingModel, SolidModel }
import lns.core.Macros.copyMacro
import lns.scenes.game.elements.stone.{ StoneModel, StoneView }
import lns.scenes.game.room.{ Cell, Floor }

import scala.util.Random

trait ElementModel extends SolidModel {
  type Model >: this.type <: ElementModel

  val crossable: Boolean

  def withSolid(crossable: Boolean): Model
}

object ElementModel {

  def stone(cell: Cell) = StoneModel(
    id = AnythingId.generate,
    view = () => StoneView,
    boundingBox = StoneView.boundingBox(Vertex(Rooms.cellSize * cell.x, Rooms.cellSize * cell.y)),
    shotAreaOffset = 0
  )

  val elementModels = Seq(stone)

  def random(cells: Seq[Cell]): Map[AnythingId, AnythingModel] =
    def _random(cells: Seq[Cell], stones: Map[AnythingId, AnythingModel]): Map[AnythingId, AnythingModel] =
      if cells.size <= 0 then stones
      else {
        val element = Random.shuffle(elementModels).head(cells.head)
        _random(cells.tail, stones + (element.id -> element))
      }

    val randomStonesNumber =
      val max = 15
      val min = 5
      cells.size match {
        case n if n > max + 1 => Random.between(min, max)
        case n if n > min + 1 => n
        case 0                => 0
        case n                => Random.between(0, n)
      }

    _random(cells.take(randomStonesNumber), Map.empty)
}
