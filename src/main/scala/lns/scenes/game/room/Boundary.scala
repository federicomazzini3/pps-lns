package lns.scenes.game.room

import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.room.door.Location
import lns.scenes.game.room.door.Location.*
import org.scalajs.dom.raw.Position

object Boundary {

  /**
   * calculates, for an element, its new position inside a bounding box which contains it
   * @param container
   *   the bounding box that must contain the point
   * @param elem
   *   the element that may not be inside the bounding box
   * @return
   *   a new position inside the bounding box, shifted by the minimum necessary on the x and y axis
   */
  def bound(container: BoundingBox, elem: BoundingBox): Vertex = {

    /**
     * a point is update with the edge coordinate (left or right) of the bounding box if it's beyond
     * @return
     *   the new x coordinate
     */
    def xBounded: Double =
      if beyond(container, elem)(Left) then container.left
      else if beyond(container, elem)(Right) then container.right - elem.width
      else elem.position.x

    /**
     * a point is update with the edge coordinate (top or bottom) of the bounding box if it's beyond
     * @return
     *   the new y coordinate
     */
    def yBounded: Double =
      if beyond(container, elem)(Above) then container.top - elem.height
      else if beyond(container, elem)(Below) then container.bottom - elem.height
      else elem.position.y

    Vertex(xBounded, yBounded)
  }

  /**
   * Check, for an element, if it is beyond the boundaries of the object that contains it
   * @param container
   *   the element's container
   * @param elem
   *   the element inside
   * @param location
   *   the specific edge to check
   * @return
   *   if the element is beyond its container
   */
  def beyond(container: BoundingBox, elem: BoundingBox)(location: Location): Boolean =
    location match {
      case Location.Left  => container.left >= elem.left
      case Location.Right => container.right <= elem.right
      case Location.Above => container.top >= elem.bottom
      case Location.Below => container.bottom <= elem.bottom
    }

  /**
   * Check, for an element, if it's center aligned in a specific edge of its container
   * @param container
   *   the element's container
   * @param elem
   *   the element inside
   * @param location
   *   the specific edge to check
   * @return
   *   if the element is center aligned
   */
  def centerAligned(container: BoundingBox, elem: BoundingBox)(location: Location): Boolean =
    location match {
      case Left | Right  => container.verticalCenter > elem.top && container.verticalCenter < elem.bottom
      case Above | Below => container.horizontalCenter > elem.left && container.horizontalCenter < elem.right
    }
}
