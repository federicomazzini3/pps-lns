package lns.scenes.game.stats

import indigo.scenes.Lens

final case class Stats(
    val maxLife: Int,
    val invincibility: Double,
    val maxSpeed: Int,
    val damage: Double,
    val fireDamage: Double,
    val fireRange: Int,
    val fireRate: Double
)

object StatsLens {
  def maxLife: Lens[Stats, Int]          = Lens(_.maxLife, (s, v) => s.copy(maxLife = v))
  def invincibility: Lens[Stats, Double] = Lens(_.invincibility, (s, v) => s.copy(invincibility = v))
  def maxSpeed: Lens[Stats, Int]         = Lens(_.maxSpeed, (s, v) => s.copy(maxSpeed = v))
  def damage: Lens[Stats, Double]        = Lens(_.damage, (s, v) => s.copy(damage = v))
  def fireDamage: Lens[Stats, Double]    = Lens(_.fireDamage, (s, v) => s.copy(fireDamage = v))
  def fireRange: Lens[Stats, Int]        = Lens(_.fireRange, (s, v) => s.copy(fireRange = v))
  def fireRate: Lens[Stats, Double]      = Lens(_.fireRate, (s, v) => s.copy(fireRate = v))

  def apply[A <: Double](what: String)(stats: Stats, value: A): Stats = what match {
    case "maxLife"       => maxLife.set(stats, value.toInt)
    case "invincibility" => invincibility.set(stats, value)
    case "maxSpeed"      => maxSpeed.set(stats, value.toInt)
    case "damage"        => damage.set(stats, value)
    case "fireDamage"    => fireDamage.set(stats, value)
    case "fireRange"     => fireRange.set(stats, value.toInt)
    case "fireRate"      => fireRate.set(stats, value)
  }
}

object Stats {
  def Isaac: Stats = Stats(
    maxLife = 10,
    invincibility = 1.5,
    maxSpeed = 300,
    damage = 0,
    fireDamage = 3,
    fireRange = 500,
    fireRate = 0.4
  )
}
