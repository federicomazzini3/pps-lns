package lns.scenes.game.collisions

import indigoextras.geometry.BoundingBox
import lns.scenes.game.room.door.Location
import lns.scenes.game.room.door.Location.{ Above, Below, Left, Right }

object Collision {

  /**
   * Check if an element is beyond its container and specifically which side is beyond
   * @param container
   *   the container Bounding Box
   * @param elem
   *   the element Bounding Box
   * @return
   *   the element's side beyond the container
   */
  def withContainer(container: BoundingBox, elem: BoundingBox): (Option[Location], Option[Location]) =
    checkInside(container, elem) match {
      case false => nearestContainerEdges(container, elem)
      case _     => (None, None)
    }

  /**
   * Check if an element collide another element and specifically on which side
   * @param elem1
   *   the first element Bounding Box
   * @param elem2
   *   the second element Bounding Box
   * @return
   *   the element's side which collide each other
   */
  def withElement(elem1: BoundingBox, elem2: BoundingBox): Option[(Location, Location)] =
    checkCollision(elem1, elem2) match {
      case true => nearestCollisionEdge(elem1, elem2)
      case _    => None
    }

  /**
   * Check if an element collide with another element
   * @param elem1
   *   the first element Bounding Box
   * @param elem2
   *   the second element Bounding Box
   * @return
   *   true if elements collide
   */
  def checkCollision(elem1: BoundingBox, elem2: BoundingBox): Boolean =
    elem1.contains(elem2.topLeft) || elem1.contains(elem2.topRight) ||
      elem1.contains(elem2.bottomLeft) || elem1.contains(elem2.bottomRight) ||
      elem2.contains(elem1.topLeft) || elem2.contains(elem1.topRight) ||
      elem2.contains(elem1.bottomLeft) || elem2.contains(elem1.bottomRight)

  /**
   * Check if an element is inside its container
   * @param container
   *   the container Bounding Box
   * @param elem
   *   the element Bounding Box
   * @return
   *   true if the element is inside its container
   */
  def checkInside(container: BoundingBox, elem: BoundingBox): Boolean =
    container.left < elem.left && container.right > elem.right &&
      container.top < elem.top && container.bottom > elem.bottom

  /**
   * Find the nearest container and element edges
   * @param container
   *   the container Bounding Box
   * @param elem
   *   the element Bounding Box
   * @return
   *   the two nearest edges of the two
   */
  def nearestContainerEdges(container: BoundingBox, elem: BoundingBox): (Option[Location], Option[Location]) =
    def nearestXEdge: Option[Location] =
      if container.left >= elem.left then Some(Left)
      else if container.right <= elem.right then Some(Right)
      else None

    def nearestYEdge: Option[Location] =
      if container.top >= elem.top then Some(Above)
      else if container.bottom <= elem.bottom then Some(Below)
      else None

    (nearestXEdge, nearestYEdge)

  /**
   * Find the nearest edges of two elements
   * @param elem1
   *   the first element Bounding Box
   * @param elem2
   *   the second element Bounding Box
   * @return
   *   the two nearest edges
   */
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
