package lns.scenes.game.collisions

import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.scenes.game.room.door.Location.{ Above, Below, Left, Right }

object Boundary {

  /**
   * Force a bounding box inside a container
   * @param container
   *   the container box
   * @param elem
   *   the element to force inside the container
   * @return
   *   a new Bounding Box forced inside the container
   */
  def containerBound(container: BoundingBox, elem: BoundingBox): BoundingBox =
    val moves = Map(
      Above -> container.top,
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

  /**
   * Forces a bounding box not to intersect with another
   * @param elem1
   *   the first Bounding Box
   * @param elem2
   *   the second Bounding Box
   * @return
   *   a new Bounding Box moved the minimum necessary not to intersect with the other Bounding Box
   */
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
