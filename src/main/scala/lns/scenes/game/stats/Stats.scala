package lns.scenes.game.stats

enum PropertyName:
  case MaxLife, Invincibility, MaxSpeed, Damage, FireDamage, FireRange, FireRate

import PropertyName.*

type PropertyValue = Double
type StatProperty  = (PropertyName, PropertyValue)
type Stats         = Map[PropertyName, PropertyValue]

given Conversion[PropertyValue, Int] with
  def apply(v: PropertyValue): Int = v.toInt

given Conversion[PropertyValue, String] with
  def apply(v: PropertyValue): String = v.toString

/**
 * PropertyValue extension to allow you add value but the result cannot be less than 0
 */
extension (p: PropertyValue) {
  def |+|(v: PropertyValue): PropertyValue = p match {
    case p if (v + p) < 0 => 0
    case _                => v + p
  }
}

/**
 * PropertyName extension to allow get StatProperty If does not exist return default 0.0
 */
extension (p: PropertyName) {
  def @@(s: Stats): PropertyValue = s.getOrElse(p, 0.0)
}

/**
 * Stats extension to allow you add StatProperty to the current StatProperty value
 */
extension (stats: Stats) {
  def +++(p: StatProperty): Stats =
    stats + stats.get(p._1).map(x => p._1 -> (x |+| p._2)).getOrElse(p)
}

/**
 * Companion object to create Stats, a Map of tuples StatProperty (PropertyName, PropertyValue)
 */
object Stats {
  def apply(args: StatProperty*): Stats = Map(args*)

  def Isaac = Stats(
    MaxLife       -> 3,
    Invincibility -> 1.5,
    MaxSpeed      -> 300,
    Damage        -> 0,
    FireDamage    -> 3.0,
    FireRange     -> 500,
    FireRate      -> 0.4
  )
}
