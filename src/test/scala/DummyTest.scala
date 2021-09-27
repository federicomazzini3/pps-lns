import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.wordspec.AnyWordSpec
import org.junit.jupiter.api.Assertions.assertFalse

class FlatSpecTest extends AnyFlatSpec {

  "An empty Set" should "have size 0" in {
    assert(Set.empty.size == 0)
  }

  it should "produce NoSuchElementException when head is invoked" in {
    assertThrows[NoSuchElementException] {
      Set.empty.head
    }
  }

  "A non empty set" should "contains something" in {
    val a = Set(1, 2, 3)
    assertFalse(a.isEmpty)
  }
}

class FreeSpecTest extends AnyFreeSpec {

  "A Set" - {
    "when empty" - {
      "should have size 0" in {
        assert(Set.empty.size == 0)
      }
      "should produce NoSuchElementException when head is invoked" in {
        assertThrows[NoSuchElementException] {
          Set.empty.head
        }
      }
    }
    "when non empty" - {
      val a: Set[Int] = Set(1, 2, 3)

      "should contains something" in {
        assertFalse(a.isEmpty)
      }
    }
  }
}

class WordSpecTest extends AnyWordSpec {

  "A Set" when {
    "empty" should {
      "have size 0" in {
        assert(Set.empty.size == 0)
      }
      "should produce NoSuchElementException when head is invoked" in {
        assertThrows[NoSuchElementException] {
          Set.empty.head
        }
      }
    }
    "non empty" should {
      val a: Set[Int] = Set(1, 2, 3)
      "should contains something" in {
        assertFalse(a.isEmpty)
      }
    }
  }
}
