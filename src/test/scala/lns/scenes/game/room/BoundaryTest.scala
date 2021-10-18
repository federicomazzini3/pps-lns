package lns.scenes.game.room
import org.junit.jupiter.api.Assertions.{ assertEquals, assertFalse }
import org.scalatest.freespec.AnyFreeSpec
import indigoextras.geometry.{ BoundingBox, Vertex }

class BoundaryTest extends AnyFreeSpec {

  val boundingBox = BoundingBox(0, 0, 200, 200)
  "A position" - {
    "inside a bounding box" - {
      val position = Vertex(100, 100)
      "should be the same after the Bound" in {
        assertEquals(Boundary.positionBounded(boundingBox, position), position)
      }
      "moved beyond the left edge" - {
        "should be constraint on left value" in {
          val newPosition = position.moveBy(-200, 0)
          assertEquals(Boundary.positionBounded(boundingBox, newPosition), Vertex(boundingBox.left, newPosition.y))
        }
      }
      "moved beyond the right edge" - {
        "should be constraint on right value" in {
          val newPosition = position.moveBy(+200, 0)
          assertEquals(Boundary.positionBounded(boundingBox, newPosition), Vertex(boundingBox.right, newPosition.y))
        }
      }
      "moved beyond the top edge" - {
        "should be constraint on top value" in {
          val newPosition = position.moveBy(0, -200)
          assertEquals(Boundary.positionBounded(boundingBox, newPosition), Vertex(newPosition.x, boundingBox.top))
        }
      }
      "moved beyond the right edge" - {
        "should be constraint on bottom value" in {
          val newPosition = position.moveBy(0, +200)
          assertEquals(Boundary.positionBounded(boundingBox, newPosition), Vertex(newPosition.x, boundingBox.bottom))
        }
      }
    }
  }
}
