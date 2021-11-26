package lns.scenes.game.enemies.mask

import scala.language.implicitConversions
import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.*
import lns.scenes.game.anything.given
import lns.scenes.game.enemies.{ EnemyModel, EnemyState, EnemyStatus, FiresContinuously, KeepsAway }
import lns.scenes.game.room.Cell
import lns.scenes.game.shots.*
import lns.scenes.game.stats.*
import lns.scenes.game.stats.given
import lns.scenes.game.stats.PropertyName.*

import scala.collection.immutable.Queue

/**
 * Enemy model that is alive, it's dynamic by computing its speed and new position by a defined strategy, can fire
 * computing shot and have stats
 * @param id
 *   [[AnythingId]] The unique identifier of the Anything instance.
 * @param view
 *   [[AnythingView]] The Anything's View factory function.
 * @param boundingBox
 *   [[AnythingModel]] boundingBox
 * @param shotAreaOffset
 *   shotAreaOffset
 * @param stats
 *   Initial [[Stats]]
 * @param status
 *   Initial [[EnemyState]]
 * @param crossable
 *   crossable, default false
 * @param speed
 *   [[DynamicModel]] speed, default Vector2(0, 0)
 * @param collisionDetected
 *   [[DynamicModel]] collisionDetected, true if the Anything is collided with some other Anything. Default false
 * @param life
 *   [[AliveModel]] life, default 0
 * @param invincibilityTimer
 *   [[AliveModel]] invincibilityTimer, default 0
 * @param fireRateTimer
 *   [[FireModel]] fireRateTimer, default 0
 * @param shots
 *   [[FireModel]] shots, default None
 */
case class MaskModel(
    id: AnythingId,
    view: () => MaskView[_],
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Attacking, 0, None)),
    crossable: Boolean = false,
    speed: Vector2 = Vector2(0, 0),
    collisionDetected: Boolean = false,
    life: Double = 0,
    invincibilityTimer: Timer = 0,
    fireRateTimer: Timer = 0,
    shots: Option[List[Vector2]] = None
) extends EnemyModel
    with DynamicModel
    with FireModel
    with KeepsAway
    with FiresContinuously {

  type Model = MaskModel

  val shotView   = () => new SingleShotView() with ShotRed
  val shotOffset = boundingBox.height / 2

  def withStats(stats: Stats): Model                                                           = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model                                            = copyMacro
  def withAlive(life: Double, invincibilityTimer: Double): Model                               = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2, collisionDetected: Boolean): Model = copyMacro
  def withFire(fireRateTimer: Double, shots: Option[List[Vector2]]): Model                     = copyMacro
  def withSolid(crossable: Boolean): Model                                                     = copyMacro
}

/**
 * Factory of [[MaskModel]]
 */
object MaskModel {

  def initial(cell: Cell): MaskModel = MaskModel(
    AnythingId.generate,
    view = () => MaskView,
    boundingBox = MaskView.boundingBox(Vertex(Assets.Rooms.cellSize * cell.x, Assets.Rooms.cellSize * cell.y)),
    shotAreaOffset = MaskView.shotAreaOffset,
    stats = Stats.Mask,
    life = MaxLife @@ Stats.Mask
  )
}
