package lns.scenes.game.enemies.nerve

import scala.language.implicitConversions
import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.*
import lns.scenes.game.enemies.{ EnemyModel, EnemyState, EnemyStatus, FiresContinuously, KeepsAway }
import lns.scenes.game.room.{ Cell, RoomModel }
import lns.scenes.game.shots.ShotEvent
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.MaxLife

import scala.collection.immutable.Queue

/**
 * Enemy model that is alive and stay fixed in a position
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

  def initial(cell: Cell): NerveModel = NerveModel(
    AnythingId.generate,
    view = () => NerveView,
    boundingBox = NerveView.boundingBox(Vertex(Assets.Rooms.cellSize * cell.x, Assets.Rooms.cellSize * cell.y)),
    shotAreaOffset = NerveView.shotAreaOffset,
    stats = Stats.Nerve,
    life = MaxLife @@ Stats.Nerve
  )
}
