package lns.scenes.game.enemy.boney

import scala.language.implicitConversions
import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.*
import lns.scenes.game.room.RoomModel
import lns.scenes.game.shot.ShotEvent
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*
import lns.scenes.game.enemy.{ EnemyModel, EnemyState, EnemyStatus, Follower }

import scala.collection.immutable.Queue

/**
 * Enemy model that is alive, it's dynamic by computing its speed and new position by a defined strategy, can fire
 * computing shot and have stats
 *
 * @param boundingBox
 *   [[AnythingModel]] boundingBox
 * @param status
 *   Initial [[EnemyState]]
 * @param stats
 *   Initial [[Stats]]
 * @param speed
 *   [[DynamicModel]] speed, default Vector2(0, 0)
 * @param life
 *   [[AliveModel]] life, default 0
 * @param invincibilityTimer
 *   [[AliveModel]] invincibilityTimer, default 0
 */
case class BoneyModel(
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Attacking, 0)),
    val crossable: Boolean = false,
    speed: Vector2 = Vector2(0, 0),
    life: Int = 0,
    invincibilityTimer: Timer = 0
) extends EnemyModel
    with DynamicModel
    with Follower {

  type Model = BoneyModel

  def withStats(stats: Stats): Model                               = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model                = copyMacro
  def withAlive(life: Int, invincibilityTimer: Double): Model      = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro
}

/**
 * Factory of [[BoneyModel]]
 */
object BoneyModel {
  import Assets.Enemies.Boney.*

  def initial: BoneyModel = BoneyModel(
    boundingBox = BoundingBox(
      Vertex(Assets.Rooms.floorSize / 2, Assets.Rooms.floorSize / 2),
      Vertex(
        withScale(width),
        withScale(height - offsetY)
      )
    ),
    shotAreaOffset = offsetY,
    stats = Stats.Isaac,
    life = MaxLife @@ Stats.Isaac
  )
}
