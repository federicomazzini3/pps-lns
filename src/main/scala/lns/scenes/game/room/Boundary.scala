package lns.scenes.game.room

import indigoextras.geometry.{ BoundingBox, Vertex }
import org.scalajs.dom.raw.Position

object Boundary {

  def positionBounded(floor: BoundingBox, position: Vertex): Vertex = {

    def xBounded: Double =
      if position.x <= floor.left then floor.left
      else if position.x > floor.right then floor.right
      else position.x

    def yBounded: Double =
      if position.y <= floor.top then floor.top
      else if position.y > floor.bottom then floor.bottom
      else position.y

    Vertex(xBounded, yBounded)
  }
}
