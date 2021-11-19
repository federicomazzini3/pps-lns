package lns.scenes.game.stats

import PropertyName.*

/**
 * Characters stats to mixin with Stats companion object
 */
trait Characters {

  def Isaac: Stats = Stats(
    MaxLife       -> 3,
    Invincibility -> 1.5,
    MaxSpeed      -> 600,
    Damage        -> 0,
    FireDamage    -> 1,
    FireRange     -> 900,
    FireRate      -> 0.3,
    FireSpeed     -> 1000
  )
}
