package lns.scenes.game.stats

import PropertyName.*

/**
 * Characters stats to mixin with Stats companion object
 */
trait Characters {

  def Isaac: Stats = Stats(
    MaxLife       -> 3,
    Invincibility -> 1.5,
    MaxSpeed      -> 400,
    Damage        -> 0,
    FireDamage    -> 1,
    FireRange     -> 800,
    FireRate      -> 0.4,
    FireSpeed     -> 600
  )
}
