package lns.scenes.game.room

import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.scenes.game.room.door.Location
import org.scalajs.dom.raw.Position

object Boundary {

  /**
   * calculates a new position inside a bounding box starting from a starting position
   * @param floor
   *   the bounding box that must contain the point
   * @param position
   *   the position that may not be inside the bounding box
   * @return
   *   a new position inside the bounding box, shifted by the minimum necessary on the x and y axis
   */
  def characterBounded(floor: BoundingBox, thing: BoundingBox): Vertex = {

    /**
     * a point is update with the edge coordinate (left or right) of the bounding box if it's beyond
     * @return
     *   the new coordinate
     */
    def xBounded: Double =
      if thing.bottomLeft.x <= floor.left then floor.left
      else if thing.bottomRight.x > floor.right then floor.right - thing.width
      else thing.position.x

    /**
     * a point is update with the edge coordinate (top or bottom) of the bounding box if it's beyond
     * @return
     *   the new coordinate
     */
    def yBounded: Double =
      if thing.bottomLeft.y <= floor.top then floor.top - thing.height
      else if thing.bottomLeft.y > floor.bottom then floor.bottom - thing.height
      else thing.position.y

    Vertex(xBounded, yBounded)
  }

  def onBound(floor: BoundingBox, thing: BoundingBox)(location: Location): Boolean = location match {
    case Location.Left  => floor.left == thing.left
    case Location.Right => floor.right == thing.right
    case Location.Above => floor.top == thing.top
    case Location.Below => floor.bottom == thing.bottom
    case null           => false
  }
}
