package lns.scenes.game.room

import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.room.door.Location
import lns.scenes.game.room.door.Location.*
import org.scalajs.dom.raw.Position

object Boundary {

  /**
   * calculates a new position inside a bounding box starting from a starting position
   * @param container
   *   the bounding box that must contain the point
   * @param position
   *   the position that may not be inside the bounding box
   * @return
   *   a new position inside the bounding box, shifted by the minimum necessary on the x and y axis
   */
  def bound(container: BoundingBox, thing: BoundingBox): Vertex = {

    /**
     * a point is update with the edge coordinate (left or right) of the bounding box if it's beyond
     * @return
     *   the new coordinate
     */
    def xBounded: Double =
      if beyond(container, thing)(Left) then container.left
      else if beyond(container, thing)(Right) then container.right - thing.width
      else thing.position.x

    /**
     * a point is update with the edge coordinate (top or bottom) of the bounding box if it's beyond
     * @return
     *   the new coordinate
     */
    def yBounded: Double =
      if beyond(container, thing)(Above) then container.top - thing.height
      else if beyond(container, thing)(Below) then container.bottom - thing.height
      else thing.position.y

    Vertex(xBounded, yBounded)
  }

  def beyond(container: BoundingBox, thing: BoundingBox)(location: Location): Boolean =
    location match {
      case Location.Left => println("Left: " + container.left + " " + thing.left); container.left >= thing.left
      case Location.Right =>
        println("Right: " + container.right + " " + thing.right); container.right <= thing.right
      case Location.Above => println("Above: " + container.top + " " + thing.bottom); container.top >= thing.bottom
      case Location.Below =>
        println("Below: " + container.bottom + " " + thing.bottom); container.bottom <= thing.bottom
    }

  def centerAligned(container: BoundingBox, thing: BoundingBox)(location: Location): Boolean =
    location match {
      case Left | Right  => container.verticalCenter > thing.top && container.verticalCenter < thing.bottom
      case Above | Below => container.horizontalCenter > thing.left && container.horizontalCenter < thing.right
    }
}

object CharacterExtension {
  extension (character: CharacterModel) {
    def boundMovement(floor: BoundingBox): CharacterModel =
      character.copy(
        boundingBox = character.boundingBox.moveTo(Boundary.bound(floor, character.boundingBox))
      )
  }
}
