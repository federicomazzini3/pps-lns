package lns.scenes.game.enemy.nerve

import scala.language.implicitConversions
import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.*
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.enemy.{ EnemyModel, EnemyState, FiresContinuously, KeepsAway }
import lns.scenes.game.room.{ Boundary, RoomModel }
import lns.scenes.game.shot.ShotEvent
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.MaxLife

/**
 * Enemy model that is alive and stay fixed in a position
 *
 * @param boundingBox
 *   [[AnythingModel]] boundingBox
 * @param status
 *   Initial [[EnemyState]]
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
    status: EnemyState = EnemyState.Attacking,
    life: Int = 0,
    invincibilityTimer: Timer = 0
) extends EnemyModel {

  type Model = NerveModel

  def withStats(stats: Stats): Model                          = copyMacro
  def withStatus(status: EnemyState): Model                   = copyMacro
  def withAlive(life: Int, invincibilityTimer: Double): Model = copyMacro
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
    life = MaxLife @@ Stats.Isaac
  )
}
