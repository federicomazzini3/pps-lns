package lns.scenes.game.enemy.mask

import scala.language.implicitConversions
import indigo.*
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ *, given }
import lns.scenes.game.enemy.{ EnemyModel, EnemyState, EnemyStatus, FiresContinuously, KeepsAway }
import lns.scenes.game.shot.ShotEvent
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

import scala.collection.immutable.Queue

/**
 * Enemy model that is alive, it's dynamic by computing its speed and new position by a defined strategy, can fire
 * computing shot and have stats
 *
 * @param boundingBox
 *   [[AnythingModel]] boundingBox
 * @param stats
 *   Initial [[Stats]]
 * @param status
 *   Initial [[EnemyState]]
 * @param speed
 *   [[DynamicModel]] speed, default Vector2(0, 0)
 * @param life
 *   [[AliveModel]] life, default 0
 * @param invincibilityTimer
 *   [[AliveModel]] invincibilityTimer, default 0
 * @param fireRateTimer
 *   [[FireModel]] fireRateTimer, default 0
 * @param shot
 *   [[FireModel]] shot, default None
 */
case class MaskModel(
    id: AnythingId,
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Attacking, 0)),
    val crossable: Boolean = false,
    speed: Vector2 = Vector2(0, 0),
    life: Int = 0,
    invincibilityTimer: Timer = 0,
    fireRateTimer: Timer = 0,
    shot: Option[Vector2] = None
) extends EnemyModel
    with DynamicModel
    with FireModel
    with KeepsAway((600, 900))
    with FiresContinuously {

  type Model = MaskModel

  val shotOffset = boundingBox.height / 2

  def withStats(stats: Stats): Model                                = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model                 = copyMacro
  def withAlive(life: Int, invincibilityTimer: Double): Model       = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model  = copyMacro
  def withFire(fireRateTimer: Double, shot: Option[Vector2]): Model = copyMacro
}

/**
 * Factory of [[MaskModel]]
 */
object MaskModel {
  import Assets.Enemies.Mask.*
  def initial: MaskModel = MaskModel(
    AnythingId.generate,
    boundingBox = BoundingBox(
      Vector2(Assets.Rooms.floorSize / 2, Assets.Rooms.floorSize / 2),
      Vector2(
        withScale(width),
        withScale(height - offsetY)
      )
    ),
    shotAreaOffset = withScale(-offsetY),
    stats = Stats.Mask,
    life = MaxLife @@ Stats.Mask
  )
}
