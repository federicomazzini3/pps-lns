package lns.scenes.game.room.door

import indigoextras.geometry.BoundingBox
import lns.scenes.game.room.door.Location
import lns.scenes.game.room.door.Location.{ Above, Below, Left, Right }
import lns.scenes.game.room.door.LocationImplicit.opposite

object Collision {

  def check(elem1: BoundingBox, elem2: BoundingBox): Boolean =
    elem1.contains(elem2.topLeft) || elem1.contains(elem2.topRight) ||
      elem1.contains(elem2.bottomLeft) || elem1.contains(elem2.bottomRight) ||
      elem2.contains(elem1.topLeft) || elem2.contains(elem1.topRight) ||
      elem2.contains(elem1.bottomLeft) || elem2.contains(elem1.bottomRight)

  /*def apply(elem1: BoundingBox, elem2: BoundingBox): Option[(Location, Location)] = {

    def innerCollision(location: (Location, Location)): Boolean =
      location match {
        case (Above, Below) =>
          (elem1.contains(elem2.bottomLeft) && elem1.contains(elem2.bottomRight))
        case (Below, Above) =>
          (elem1.contains(elem2.topLeft) && elem1.contains(elem2.topRight))
        case (Left, Right) =>
          (elem1.contains(elem2.topRight) && elem1.contains(elem2.bottomRight))
        case (Right, Left) =>
          (elem1.contains(elem2.topLeft) && elem1.contains(elem2.bottomLeft))
        case _ => false
      }

    def outerCollision(location: (Location, Location)): Boolean =
      location match {
        case (Above, Below) =>
          (elem2.contains(elem1.topLeft) && elem2.contains(elem1.topRight))
        case (Below, Above) =>
          (elem2.contains(elem1.bottomLeft) && elem2.contains(elem1.bottomRight))
        case (Left, Right) =>
          (elem2.contains(elem1.topLeft) && elem2.contains(elem1.bottomLeft))
        case (Right, Left) =>
          (elem2.contains(elem1.topRight) && elem2.contains(elem1.bottomRight))
        case _ => false
      }

    def vertexCollision(location: (Location, Location)): Boolean =
      location match {
        case (Above, Below) =>
          (elem1.contains(elem2.bottomLeft) && (elem1.top - elem2.bottom).abs < (elem1.right - elem2.left).abs) ||
            (elem1.contains(elem2.bottomRight) && (elem1.top - elem2.bottom).abs < (elem1.left - elem2.right).abs)
        case (Below, Above) =>
          (elem1.contains(elem2.topLeft) && (elem1.bottom - elem2.top).abs < (elem1.right - elem2.left).abs) ||
            (elem1.contains(elem2.topRight) && (elem1.bottom - elem2.top).abs < (elem1.left - elem2.right).abs)
        case (Left, Right) =>
          (elem1.contains(elem2.topRight) && (elem1.left - elem2.right).abs < (elem1.bottom - elem2.top).abs) ||
            (elem1.contains(elem2.bottomRight) && (elem1.left - elem2.right).abs < (elem1.top - elem2.bottom).abs)
        case (Right, Left) =>
          (elem1.contains(elem2.topLeft) && (elem1.right - elem2.left).abs < (elem1.bottom - elem2.top).abs) ||
            (elem1.contains(elem2.bottomLeft) && (elem1.right - elem2.left).abs < (elem1.top - elem2.bottom).abs)
        case _ => false
      }

    if innerCollision(Above, Below) || outerCollision(Above, Below) || vertexCollision(Above, Below) then
      Some(Above, Below)
    else if innerCollision(Below, Above) || outerCollision(Below, Above) || vertexCollision(Below, Above) then
      Some(Below, Above)
    else if innerCollision(Left, Right) || outerCollision(Left, Right) || vertexCollision(Left, Right) then
      Some(Left, Right)
    else if innerCollision(Right, Left) || outerCollision(Right, Left) || vertexCollision(Right, Left) then
      Some(Right, Left)
    else None
  }*/

  def apply(elem1: BoundingBox, elem2: BoundingBox): Option[(Location, Location)] = {

    def innerCollision: Option[(Location, Location)] =
      if elem1.contains(elem2.bottomLeft) && elem1.contains(elem2.topLeft) &&
        elem1.contains(elem2.bottomRight) && elem1.contains(elem2.topRight)
      then nearestContainerEdge(elem1, elem2)
      else if elem1.contains(elem2.bottomLeft) && elem1.contains(elem2.bottomRight) then Some(Above, Below)
      else if elem1.contains(elem2.topLeft) && elem1.contains(elem2.topRight) then Some(Below, Above)
      else if elem1.contains(elem2.bottomLeft) && elem1.contains(elem2.topLeft) then Some(Right, Left)
      else if elem1.contains(elem2.bottomRight) && elem1.contains(elem2.topRight) then Some(Left, Right)
      else None

    def outerCollision: Option[(Location, Location)] =
      if elem2.contains(elem1.bottomLeft) && elem2.contains(elem1.topLeft) &&
        elem2.contains(elem1.bottomRight) && elem2.contains(elem1.topRight)
      then nearestContainerEdge(elem2, elem1)
      else if elem2.contains(elem1.bottomLeft) && elem2.contains(elem1.bottomRight) then Some(Below, Above)
      else if elem2.contains(elem1.topLeft) && elem2.contains(elem1.topRight) then Some(Above, Below)
      else if elem2.contains(elem1.bottomLeft) && elem2.contains(elem1.topLeft) then Some(Left, Right)
      else if elem2.contains(elem1.bottomRight) && elem2.contains(elem1.topRight) then Some(Right, Left)
      else None

    def vertexCollision: Option[(Location, Location)] =
      if elem1.contains(elem2.bottomLeft) then nearestCollisionEdge(elem1, elem2)
      else if elem1.contains(elem2.bottomRight) then nearestCollisionEdge(elem1, elem2)
      else if elem1.contains(elem2.topLeft) then nearestCollisionEdge(elem1, elem2)
      else if elem1.contains(elem2.topRight) then nearestCollisionEdge(elem1, elem2)
      else None

    innerCollision match {
      case Some(loc) => Some(loc._1, loc._2)
      case _ =>
        outerCollision match {
          case Some(loc) => Some(loc._1, loc._2)
          case _ =>
            vertexCollision match {
              case Some(loc) => Some(loc._1, loc._2)
              case _         => None
            }
        }
    }
  }

  given locationDistanceCompare: Ordering[(Location, Double)] with
    def compare(x: (Location, Double), y: (Location, Double)) =
      x._2.compareTo(y._2)

  def nearestContainerEdge(container: BoundingBox, elem2: BoundingBox): Option[(Location, Location)] =
    val distanceToTop    = (container.top - elem2.top).abs
    val distanceToBottom = (container.bottom - elem2.bottom).abs
    val distanceToLeft   = (container.left - elem2.left).abs
    val distanceToRight  = (container.right - elem2.right).abs

    val nearest =
      Map(Above -> distanceToTop, Below -> distanceToBottom, Left -> distanceToLeft, Right -> distanceToRight).min._1
    Some(nearest, nearest.opposite)

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
