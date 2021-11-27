package lns.scenes.game.stats

import org.scalatest.{ BeforeAndAfterEach, Suite }
import org.scalatest.freespec.AnyFreeSpec
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

trait StatsFixture extends BeforeAndAfterEach { this: Suite =>

  var stats: Stats = _

  override def beforeEach() = {
    stats = Stats(
      MaxLife       -> 10,
      Invincibility -> 1.5,
      MaxSpeed      -> 300,
      Damage        -> 0,
      FireDamage    -> 3.0,
      FireRange     -> 500,
      FireRate      -> 0.4,
      FireSpeed     -> 800
    )

    super.beforeEach()
  }
}
class StatsTest extends AnyFreeSpec with StatsFixture {
  "A Stats values" - {
    "should init with variable arguments" in {
      val newStats = Stats(MaxLife -> 100, Invincibility -> 20)
      assert(newStats.getOrElse(MaxLife, fail("Undefined Model")) == 100)
      assert(newStats.getOrElse(Invincibility, fail("Undefined Model")) == 20)
      assert(newStats.get(MaxSpeed) == None)
    }
    "should get with infix '@@' operator " in {
      assert(MaxLife @@ stats == 10)
    }
    "adding a StatProperty should updates the current StatProperty" in {
      val updatedStats = stats +++ (MaxLife, 10)
      assert(MaxLife @@ updatedStats == 20)
    }
    "adding a value that would bring the current negative should not be possible" in {
      val updatedStats = stats +++ (MaxLife, -20)
      assert(MaxLife @@ updatedStats == 0)
    }
    "adding double values should maintain precision to two decimal places (rounding half up)" in {
      val updatedStats = stats +++ (Invincibility, -0.12345)
      assert(Invincibility @@ updatedStats == 1.38)

      val updatedStats2 = stats +++ (Invincibility, -0.12845)
      assert(Invincibility @@ updatedStats2 == 1.37)
    }
  }
}
