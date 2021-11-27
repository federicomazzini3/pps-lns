package lns.scenes.game.stats

import PropertyName.*

/**
 * Boss stats to mixin with Stats companion object
 */
trait Bosses {

  def Loki: Stats = Stats(
    MaxLife       -> 40,
    Invincibility -> 0,
    MaxSpeed      -> 600,
    Damage        -> 1,
    FireDamage    -> 1,
    FireRange     -> 800,
    FireRate      -> 1,
    FireSpeed     -> 1000
  )
}
