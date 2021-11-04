package lns.scenes.game.stats

import scala.language.implicitConversions

/*ENUM*/

type PropertyValue = Double
type StatProperty  = (String, PropertyValue)
type Stats         = Map[String, PropertyValue]

given Conversion[PropertyValue, Int] with
  def apply(v: PropertyValue): Int = v.toInt

extension (stats: Stats) {
  def +++(p: StatProperty): Stats =
    stats + stats.get(p._1).map(x => p._1 -> (x + p._2)).getOrElse(p)
}

object Stats {
  def apply(args: StatProperty*): Stats = Map(args*)

  def Isaac = Stats(
    "maxLife"       -> 10,
    "invincibility" -> 1.5,
    "maxSpeed"      -> 300,
    "damage"        -> 0,
    "fireDamage"    -> 3.0,
    "fireRange"     -> 500,
    "fireRate"      -> 0.4
  )
}
