package lns.scenes.game.enemy.nerve

import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.*
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.enemy.{ FiresContinuously, KeepsAway }
import lns.scenes.game.room.{ Boundary, RoomModel }
import lns.scenes.game.shot.ShotEvent
import lns.scenes.game.stats.*

/**
 * Enemy model that is alive and stay fixed in a position
 *
 * @param boundingBox
 *   [[AnythingModel]] boundingBox
 * @param stats
 *   Initial [[Stats]]
 * @param life
 *   [[AliveModel]] life, default 0
 * @param invincibilityTimer
 *   [[AliveModel]] invincibilityTimer, default 0
 */
case class NerveModel(
    boundingBox: BoundingBox,
    stats: Stats,
    life: Int = 0,
    speed: Vector2 = Vector2(0, 0),
    invincibilityTimer: Double = 0
) extends AliveModel
    with DamageModel
    with StatsModel {

  type Model = NerveModel

  val maxLife: Int          = stats.maxLife
  val invincibility: Double = stats.invincibility
  val damage: Double        = stats.damage

  def withAlive(life: Int, invincibilityTimer: Double): Model = copyMacro

  def withStats(stats: Stats): Model = copyMacro

  def withStat[A <: Double](what: String)(value: A): Model =
    copy(stats = StatsLens(what)(stats, value))

}

/**
 * Factory of [[NerveModel]]
 */
object NerveModel {
  def initial: NerveModel = NerveModel(
    boundingBox = BoundingBox(
      Vertex(Assets.Rooms.floorSize / 2, Assets.Rooms.floorSize / 2),
      Vertex(
        Assets.Enemies.Nerve.withScale(Assets.Enemies.Nerve.width),
        Assets.Enemies.Nerve.withScale(Assets.Enemies.Nerve.height)
      )
    ),
    stats = Stats.Isaac,
    life = Stats.Isaac.maxLife
  )
}
