package lns.scenes.game.room

import indigoextras.geometry.BoundingBox
import org.scalatest.freespec.AnyFreeSpec
import lns.scenes.game.room.door.Location
import lns.scenes.game.room.door.Location.*

class CollisionTest extends AnyFreeSpec {

  def checkInside(cont: BoundingBox, elem: BoundingBox)    = assert(Collision.checkInside(cont, elem) == true)
  def checkNotInside(cont: BoundingBox, elem: BoundingBox) = assert(Collision.checkInside(cont, elem) == false)
  def checkContainerCollision(cont: BoundingBox, elem: BoundingBox)(
      predicted: (Option[Location], Option[Location])
  ) = assert(Collision.withContainer(cont, elem) == predicted)
  def checkCollision(elem1: BoundingBox, elem2: BoundingBox)(predicted: Option[(Location, Location)]) =
    assert(Collision.withElement(elem1, elem2) == predicted)

  "A bounding box" - {
    val elem1 = BoundingBox(0, 0, 100, 100)
    "that doesn't intersect another bounding box" - {
      val elem2 = BoundingBox(101, 101, 100, 100)
      "shouldn't collide with it" in {
        assert(Collision.checkCollision(elem1, elem2) == false)
      }
    }
    "that intersect another bounding box" - {
      val elem2 = BoundingBox(99, 99, 100, 100)
      "should collide with it" in {
        assert(Collision.checkCollision(elem1, elem2) == true)
      }
    }
  }

  "A bounding box" - {
    val elem = BoundingBox(200, 200, 100, 100)
    "inside a bounding box" - {
      val container = BoundingBox(0, 0, 1000, 1000)
      "it should be marked as inside" in {
        checkInside(container, elem)
      }
      "moved beyond the left side" - {
        val movedElem = elem.moveTo(container.left - 1, container.height / 2)
        "it should be marked as not inside" in {
          checkNotInside(container, movedElem)
        }
        "it should signal a collision on left side" in {
          checkContainerCollision(container, movedElem)(Some(Left), None)
        }
      }
      "moved beyond the right side" - {
        val movedElem = elem.moveTo(container.right - 1, container.height / 2)
        "it should be marked as not inside" in {
          checkNotInside(container, movedElem)
        }
        "it should signal a collision on right side" in {
          checkContainerCollision(container, movedElem)(Some(Right), None)
        }
      }
      "moved beyond the top side" - {
        val movedElem = elem.moveTo(container.width / 2, container.top - elem.height - 1)
        "it should be marked as not inside" in {
          checkNotInside(container, movedElem)
        }
        "it should signal a collision on top side" in {
          checkContainerCollision(container, movedElem)(None, Some(Above))
        }
      }
      "moved beyond the bottom side" - {
        val movedElem = elem.moveTo(container.width / 2, container.bottom - 1)
        "it should be marked as not inside" in {
          checkNotInside(container, movedElem)
        }
        "it should signal a collision on bottom side" in {
          checkContainerCollision(container, movedElem)(None, Some(Below))
        }
      }
      "moved outside its container on each side" - {
        val movedElem = BoundingBox(1500, 1500, 100, 100)
        "it should be marked as not inside" in {
          checkNotInside(container, movedElem)
        }
      }
      "moved on top left corner of its container" - {
        val movedElem = elem.moveTo(container.left, container.top - elem.height)
        "should signal a collision on top left corner" in {
          checkContainerCollision(container, movedElem)(Some(Left), Some(Above))
        }
      }
      "moved on top right corner of its container" - {
        val movedElem = elem.moveTo(container.right - elem.width, -elem.height)
        "should signal a collision on top right corner" in {
          checkContainerCollision(container, movedElem)(Some(Right), Some(Above))
        }
      }
      "moved on bottom left corner of its container" - {
        val movedElem = elem.moveTo(container.bottomLeft)
        "should signal a collision on bottom left corner" in {
          checkContainerCollision(container, movedElem)(Some(Left), Some(Below))
        }
      }
      "moved on bottom right corner of its container" - {
        val movedElem = elem.moveTo(container.bottomRight)
        "should signal a collision on bottom right corner" in {
          checkContainerCollision(container, movedElem)(Some(Right), Some(Below))
        }
      }
    }
  }

  "Two bounding box" - {
    val elem1 = BoundingBox(200, 200, 100, 100)
    val elem2 = BoundingBox(0, 0, 100, 100)
    "that doesn't intersect each other" - {
      "shouldn't collide on any edge" in {
        assert(Collision.withElement(elem1, elem2) == None)
        checkCollision(elem1, elem2)(None)
      }
    }
    "that interset each other on left - right side" - {
      val movedElem2 = elem2.moveTo(elem1.left - 50, elem1.y)
      "should collide on left - right side" in {
        assert(Collision.withElement(elem1, movedElem2) == Some(Left, Right))
        checkCollision(elem1, movedElem2)(Some(Left, Right))
      }
    }
    "that interset each other on right - left side" - {
      val movedElem2 = elem2.moveTo(elem1.right - 1, elem1.y)
      "should collide on right - left side" in {
        assert(Collision.withElement(elem1, movedElem2) == Some(Right, Left))
        checkCollision(elem1, movedElem2)(Some(Right, Left))
      }
    }
    "that interset each other on top - bottom side" - {
      val movedElem2 = elem2.moveTo(elem1.x, elem1.top - 50)
      "should collide on left - right side" in {
        assert(Collision.withElement(elem1, movedElem2) == Some(Above, Below))
        checkCollision(elem1, movedElem2)(Some(Above, Below))
      }
    }
    "that interset each other on bottom - top side" - {
      val movedElem2 = elem2.moveTo(elem1.x, elem1.bottom - 1)
      "should collide on left - right side" in {
        assert(Collision.withElement(elem1, movedElem2) == Some(Below, Above))
        checkCollision(elem1, movedElem2)(Some(Below, Above))
      }
    }
  }
}
