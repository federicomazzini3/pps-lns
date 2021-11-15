package lns.scenes.game.enemy.nerve

import scala.language.implicitConversions
import indigo.*
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.{ *, given }
import lns.scenes.game.enemy.{ EnemyModel, EnemyState, EnemyStatus, FiresContinuously, KeepsAway }
import lns.scenes.game.room.RoomModel
import lns.scenes.game.shot.ShotEvent
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.MaxLife

import scala.collection.immutable.Queue

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
    id: AnythingId,
    view: () => NerveView[_],
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Attacking, 0)),
    crossable: Boolean = false,
    life: Double = 0,
    invincibilityTimer: Timer = 0
) extends EnemyModel {

  type Model = NerveModel

  def withStats(stats: Stats): Model                             = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model              = copyMacro
  def withAlive(life: Double, invincibilityTimer: Double): Model = copyMacro
  def withSolid(crossable: Boolean): Model                       = copyMacro
}

/**
 * Factory of [[NerveModel]]
 */
object NerveModel {
  import Assets.Enemies.Nerve.*

  def initial: NerveModel = NerveModel(
    AnythingId.generate,
    view = () => NerveView,
    boundingBox = BoundingBox(
      Vector2(Assets.Rooms.floorSize / 2, Assets.Rooms.floorSize / 2),
      Vector2(
        withScale(width),
        withScale(height - offsetY)
      )
    ),
    shotAreaOffset = withScale(-offsetY),
    stats = Stats.Nerve,
    life = MaxLife @@ Stats.Nerve
  )
}
