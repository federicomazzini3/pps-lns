package lns.scenes.game.room

import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.scenes.game.anything.DynamicState
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.room.door.Location
import lns.scenes.game.room.door.Location.*
import org.scalajs.dom.raw.Position

object Boundary {

  def containerBound(container: BoundingBox, elem: BoundingBox): BoundingBox =
    val moves = Map(
      Above -> (container.top - elem.height),
      Below -> (container.bottom - elem.height),
      Left  -> container.left,
      Right -> (container.right - elem.width)
    )

    Collision.withContainer(container, elem) match {
      case (Some(collisionOnX), Some(collisionOnY)) =>
        elem.moveTo(moves(collisionOnX), moves(collisionOnY))
      case (Some(collisionOnX), None) =>
        elem.moveTo(moves(collisionOnX), elem.y)
      case (None, Some(collisionOnY)) =>
        elem.moveTo(elem.x, moves(collisionOnY))
      case _ => elem
    }

  def elementBound(elem1: BoundingBox, elem2: BoundingBox): BoundingBox =
    val moves = Map(
      Below -> Vertex(elem2.x, elem1.top - elem2.height),
      Above -> Vertex(elem2.x, elem1.bottom),
      Left  -> Vertex(elem1.right, elem2.y),
      Right -> Vertex(elem1.left - elem2.width, elem2.y)
    )

    Collision.withElement(elem1, elem2) match {
      case Some(_, collisionLocation) => elem2.moveTo(moves(collisionLocation))
      case _                          => elem2
    }
}
