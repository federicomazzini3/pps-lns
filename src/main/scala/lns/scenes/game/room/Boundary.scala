package lns.scenes.game.room

import indigoextras.geometry.{ BoundingBox, Vertex }
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
  def positionBounded(floor: BoundingBox, position: Vertex): Vertex = {

    /**
     * a point is update with the edge coordinate (left or right) of the bounding box if it's beyond
     * @return
     *   the new coordinate
     */
    def xBounded: Double =
      if position.x <= floor.left then floor.left
      else if position.x > floor.right then floor.right
      else position.x

    /**
     * a point is update with the edge coordinate (top or bottom) of the bounding box if it's beyond
     * @return
     *   the new coordinate
     */
    def yBounded: Double =
      if position.y <= floor.top then floor.top
      else if position.y > floor.bottom then floor.bottom
      else position.y

    Vertex(xBounded, yBounded)
  }
}
