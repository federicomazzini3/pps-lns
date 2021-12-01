package lns.scenes.game.collisions

import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.scenes.game.collisions.Boundary
import lns.scenes.game.room.door.Location.*
import org.scalatest.freespec.AnyFreeSpec

class BoundaryTest extends AnyFreeSpec {

  val container = BoundingBox(0, 0, 200, 200)
  "An element" - {
    "inside a bounding box" - {
      val inside = BoundingBox(100, 100, 1, 1)
      "should be located the same after the Bound" in {
        assert(inside.position == Boundary.containerBound(container, inside).position)
      }
      "moved beyond the left edge" - {
        "should be constraint on left value" in {
          val newPosition = inside.moveBy(-200, 0)
          assert(newPosition.moveTo(container.left, newPosition.y) == Boundary.containerBound(container, newPosition))
        }
      }
      "moved beyond the right edge" - {
        "should be constraint on right value" in {
          val newPosition = inside.moveBy(+200, 0)
          assert(
            newPosition
              .moveTo(container.right - newPosition.width, newPosition.y) == // the original position is in top left
              Boundary.containerBound(container, newPosition)
          )
        }
      }
      "moved beyond the top edge" - {
        "should be constraint on top value" in {
          val newPosition = inside.moveBy(0, -200)
          assert(
            newPosition
              .moveTo(newPosition.x, container.top) == // the original position is in top left
              Boundary.containerBound(container, newPosition)
          )
        }
      }
      "moved beyond the bottom edge" - {
        "should be constraint on bottom value" in {
          val newPosition = inside.moveBy(0, +200)
          assert(
            newPosition
              .moveTo(newPosition.x, container.bottom - newPosition.height) == //the original position is in top left
              Boundary.containerBound(container, newPosition)
          )
        }
      }
    }
  }

  val element = BoundingBox(0, 0, 100, 100)
  "one element" - {
    "that doesn't collide with another element" - {
      val block = BoundingBox(101, 101, 50, 50)
      "should be located the same after the bounding" in {
        //assert(element.position == Boundary.elementBound(block, element)(Above).position)
      }
    }
    "that collide on bottom side with another element" - {
      val block = BoundingBox(0, 99, 50, 50)
      "should be adjacent on bottom side" in {
        assert(block.top == Boundary.elementBound(block, element).bottom)
      }
    }
    "that collide on top side with another element" - {
      val block = BoundingBox(0, 1 - 50, 50, 50)
      "should be adjacent on top side" in {
        assert(block.bottom == Boundary.elementBound(block, element).top)
      }
    }
    "that collide on left side with another element" - {
      val block = BoundingBox(1 - 50, 0, 50, 50)
      "should be adjacent on bottom side" in {
        assert(block.right == Boundary.elementBound(block, element).left)
      }
    }
    "that collide on right side with another element" - {
      val block = BoundingBox(99, 0, 50, 50)
      "should be adjacent on right side" in {
        assert(block.left == Boundary.elementBound(block, element).right)
      }
    }
  }
}
