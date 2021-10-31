package lns.scenes.game.room
import org.scalatest.freespec.AnyFreeSpec
import indigoextras.geometry.{ BoundingBox, Vertex }

class BoundaryTest extends AnyFreeSpec {

  val container = BoundingBox(0, 0, 200, 200)
  "A position" - {
    "inside a bounding box" - {
      val inside = BoundingBox(100, 100, 1, 1)
      "should be the same after the Bound" in {
        assert(Vertex(100, 100) == Boundary.bound(container, inside))
      }
      "moved beyond the left edge" - {
        "should be constraint on left value" in {
          val newPosition = inside.moveBy(-200, 0)
          assert(Vertex(container.left, newPosition.y) == Boundary.bound(container, newPosition))
        }
      }
      "moved beyond the right edge" - {
        "should be constraint on right value" in {
          val newPosition = inside.moveBy(+200, 0)
          assert(
            Vertex(container.right - newPosition.width, newPosition.y) == // the original position is in top left
              Boundary.bound(container, newPosition)
          )
        }
      }
      "moved beyond the top edge" - {
        "should be constraint on top value" in {
          val newPosition = inside.moveBy(0, -200)
          assert(
            Vertex(newPosition.x, container.top - newPosition.height) == // the original position is in top left
              Boundary.bound(container, newPosition)
          )
        }
      }
      "moved beyond the bottom edge" - {
        "should be constraint on bottom value" in {
          val newPosition = inside.moveBy(0, +200)
          assert(
            Vertex(newPosition.x, container.bottom - newPosition.height) == //the original position is in top left
              Boundary.bound(container, newPosition)
          )
        }
      }
    }
  }
}
