package lns.scenes.game.stats

import PropertyName.*

/**
 * Enemies stats to mixin with Stats companion object
 */
trait Enemies {

  def Boney: Stats = Stats(
    MaxLife       -> 5,
    Invincibility -> 0,
    MaxSpeed      -> 300,
    Damage        -> 0.5
  )

  def Mask: Stats = Stats(
    MaxLife       -> 4,
    Invincibility -> 0,
    MaxSpeed      -> 200,
    Damage        -> 0.5,
    FireDamage    -> 0.5,
    FireRange     -> 300,
    FireRate      -> 1,
    FireSpeed     -> 600
  )

  def Nerve: Stats = Stats(
    MaxLife       -> 6,
    Invincibility -> 0,
    MaxSpeed      -> 0,
    Damage        -> 1
  )

  def Parabite: Stats = Stats(
    MaxLife       -> 3,
    Invincibility -> 0,
    MaxSpeed      -> 600,
    Damage        -> 1
  )
}
