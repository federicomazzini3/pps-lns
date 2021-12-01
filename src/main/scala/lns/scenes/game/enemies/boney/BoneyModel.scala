package lns.scenes.game.enemies.boney

import scala.language.implicitConversions
import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.*
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.*
import lns.scenes.game.enemies.{ EnemyModel, EnemyState, EnemyStatus, Follower }
import lns.scenes.game.room.Cell

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
 */
case class BoneyModel(
    id: AnythingId,
    view: () => BoneyView[_],
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Attacking, 0, None)),
    crossable: Boolean = false,
    speed: Vector2 = Vector2(0, 0),
    collisionDetected: Boolean = false,
    life: Double = 0,
    invincibilityTimer: Timer = 0
) extends EnemyModel
    with DynamicModel
    with Follower {

  type Model = BoneyModel

  def withStats(stats: Stats): Model                                                           = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model                                            = copyMacro
  def withAlive(life: Double, invincibilityTimer: Double): Model                               = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2, collisionDetected: Boolean): Model = copyMacro
  def withSolid(crossable: Boolean): Model                                                     = copyMacro
}

/**
 * Factory of [[BoneyModel]]
 */
object BoneyModel {

  def initial(cell: Cell): BoneyModel = BoneyModel(
    AnythingId.generate,
    view = () => BoneyView,
    boundingBox = BoneyView.boundingBox(Vertex(Assets.Rooms.cellSize * cell.x, Assets.Rooms.cellSize * cell.y)),
    shotAreaOffset = BoneyView.shotAreaOffset,
    stats = Stats.Boney,
    life = MaxLife @@ Stats.Boney
  )
}
