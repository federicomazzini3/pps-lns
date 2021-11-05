package lns.scenes.game.room

import indigoextras.geometry.BoundingBox
import lns.scenes.game.anything.DynamicState
import DynamicState.*
import lns.scenes.game.room.door.Location
import lns.scenes.game.room.door.Location.{ Above, Below, Left, Right }
import lns.scenes.game.room.door.LocationImplicit.opposite

object Collision {

  def withContainer(container: BoundingBox, elem: BoundingBox): (Option[Location], Option[Location]) =
    checkInside(container, elem) match {
      case false => nearestContainerEdges(container, elem)
      case _     => (None, None)
    }

  def withElement(elem1: BoundingBox, elem2: BoundingBox): Option[(Location, Location)] =
    checkCollision(elem1, elem2) match {
      case true => nearestCollisionEdge(elem1, elem2)
      case _    => None
    }

  def checkCollision(elem1: BoundingBox, elem2: BoundingBox): Boolean =
    elem1.contains(elem2.topLeft) || elem1.contains(elem2.topRight) ||
      elem1.contains(elem2.bottomLeft) || elem1.contains(elem2.bottomRight) ||
      elem2.contains(elem1.topLeft) || elem2.contains(elem1.topRight) ||
      elem2.contains(elem1.bottomLeft) || elem2.contains(elem1.bottomRight)

  def checkInside(container: BoundingBox, elem: BoundingBox): Boolean =
    container.left >= elem.left && container.right <= elem.right &&
      container.top >= elem.bottom && container.bottom <= elem.bottom

  def nearestContainerEdges(container: BoundingBox, elem: BoundingBox): (Option[Location], Option[Location]) =
    def nearestXEdge: Option[Location] =
      if container.left >= elem.left then Some(Left)
      else if container.right <= elem.right then Some(Right)
      else None

    def nearestYEdge: Option[Location] =
      if container.top >= elem.bottom then Some(Above)
      else if container.bottom <= elem.bottom then Some(Below)
      else None

    (nearestXEdge, nearestYEdge)

  def nearestCollisionEdge(elem1: BoundingBox, elem2: BoundingBox): Option[(Location, Location)] =
    val distanceToTop    = (elem1.top - elem2.bottom).abs
    val distanceToBottom = (elem1.bottom - elem2.top).abs
    val distanceToLeft   = (elem1.left - elem2.right).abs
    val distanceToRight  = (elem1.right - elem2.left).abs

    if distanceToTop < distanceToLeft && distanceToTop < distanceToRight then Some(Above, Below)
    else if distanceToBottom < distanceToLeft && distanceToBottom < distanceToRight then Some(Below, Above)
    else if distanceToLeft < distanceToTop && distanceToLeft < distanceToBottom then Some(Left, Right)
    else if distanceToRight < distanceToTop && distanceToRight < distanceToBottom then Some(Right, Left)
    else None
}
