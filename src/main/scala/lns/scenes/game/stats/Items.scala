package lns.scenes.game.stats

import PropertyName.*

/**
 * Items stats to mixin with Stats companion object
 */
trait Items {

  /**
   * Get item's stats by name
   * @param name
   *   Item name
   */
  def item(name: String): Stats = itemsStat.getOrElse(name, Stats())

  val itemsStat = Map(
    "arrow"    -> Stats(FireRate -> -0.1),
    "drop"     -> Stats(FireDamage -> 1),
    "eye"      -> Stats(FireDamage -> 1, FireRate -> -0.1),
    "fireball" -> Stats(FireDamage -> 1, FireSpeed -> 200),
    "glasses"  -> Stats(FireRange -> 200),
    "heart"    -> Stats(MaxLife -> 1),
    "juice"    -> Stats(MaxSpeed -> 200, FireDamage -> 1),
    "mushroom" -> Stats(MaxLife -> 2),
    "syringe"  -> Stats(MaxSpeed -> 200, FireRate -> -0.1, FireSpeed -> 200),
    "tail"     -> Stats(FireDamage -> 1, FireRange -> 200)
  )
}
