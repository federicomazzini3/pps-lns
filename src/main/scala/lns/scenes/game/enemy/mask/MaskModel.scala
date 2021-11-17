package lns.scenes.game.enemy.mask

import scala.language.implicitConversions
import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
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
    view: () => MaskView[_],
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Attacking, 0)),
    crossable: Boolean = false,
    speed: Vector2 = Vector2(0, 0),
    life: Double = 0,
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
  def withAlive(life: Double, invincibilityTimer: Double): Model    = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model  = copyMacro
  def withFire(fireRateTimer: Double, shot: Option[Vector2]): Model = copyMacro
  def withSolid(crossable: Boolean): Model                          = copyMacro
}

/**
 * Factory of [[MaskModel]]
 */
object MaskModel {

  def initial: MaskModel = MaskModel(
    AnythingId.generate,
    view = () => MaskView,
    boundingBox = MaskView.boundingBox(Vertex(Assets.Rooms.floorSize / 2, Assets.Rooms.floorSize / 2)),
    shotAreaOffset = MaskView.shotAreaOffset,
    stats = Stats.Mask,
    life = MaxLife @@ Stats.Mask
  )

  def initial2: MaskModel = MaskModel(
    AnythingId.generate,
    view = () => MaskView,
    boundingBox = MaskView.boundingBox(Vertex(Assets.Rooms.floorSize / 6, Assets.Rooms.floorSize / 6)),
    shotAreaOffset = MaskView.shotAreaOffset,
    stats = Stats.Mask,
    life = MaxLife @@ Stats.Mask
  )
}
