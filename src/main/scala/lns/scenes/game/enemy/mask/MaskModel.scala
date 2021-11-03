package lns.scenes.game.enemy.mask

import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.*
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.enemy.{ KeepsAway, FiresContinuously }
import lns.scenes.game.room.{ Boundary, RoomModel }
import lns.scenes.game.shot.ShotEvent
import lns.scenes.game.stats.*

/**
 * Enemy model that is alive, it's dynamic by computing its speed and new position by a defined strategy, can fire
 * computing shot and have stats
 *
 * @param boundingBox
 *   [[AnythingModel]] boundingBox
 * @param stats
 *   Initial [[Stats]]
 * @param life
 *   [[AliveModel]] life, default 0
 * @param speed
 *   [[DynamicModel]] speed, default Vector2(0, 0)
 * @param invincibilityTimer
 *   [[AliveModel]] invincibilityTimer, default 0
 * @param fireRateTimer
 *   [[FireModel]] fireRateTimer, default 0
 * @param shot
 *   [[FireModel]] shot, default None
 */
case class MaskModel(
    boundingBox: BoundingBox,
    stats: Stats,
    life: Int = 0,
    speed: Vector2 = Vector2(0, 0),
    invincibilityTimer: Double = 0,
    fireRateTimer: Double = 0,
    shot: Option[Vector2] = None
) extends AliveModel
    with DynamicModel
    with DamageModel
    with StatsModel
    with FireModel
    with KeepsAway(stats.maxSpeed, (600, 900))
    with FiresContinuously {

  type Model = MaskModel

  val maxLife: Int          = stats.maxLife
  val invincibility: Double = stats.invincibility
  // val maxSpeed: Int         = stats.maxSpeed
  val damage: Double     = stats.damage
  val fireDamage: Double = stats.fireDamage
  val fireRange: Int     = stats.fireRange
  val fireRate: Double   = stats.fireRate

  def withAlive(life: Int, invincibilityTimer: Double): Model       = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model  = copyMacro
  def withFire(fireRateTimer: Double, shot: Option[Vector2]): Model = copyMacro
  def withStats(stats: Stats): Model                                = copyMacro
  def withStat[A <: Double](what: String)(value: A): Model =
    copy(stats = StatsLens(what)(stats, value))

}

/**
 * Factory of [[MaskModel]]
 */
object MaskModel {
  def initial: MaskModel = MaskModel(
    boundingBox = BoundingBox(
      Vertex(Assets.Rooms.floorSize / 2, Assets.Rooms.floorSize / 2),
      Vertex(
        Assets.Enemies.Mask.withScale(Assets.Enemies.Mask.width),
        Assets.Enemies.Mask.withScale(Assets.Enemies.Mask.height)
      )
    ),
    stats = Stats.Isaac,
    life = Stats.Isaac.maxLife
  )
}
