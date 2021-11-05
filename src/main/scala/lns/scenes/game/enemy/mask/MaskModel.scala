package lns.scenes.game.enemy.mask

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
import lns.scenes.game.stats.PropertyName.*

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
    boundingBox: BoundingBox,
    stats: Stats,
    status: EnemyState = EnemyState.Attacking,
    speed: Vector2 = Vector2(0, 0),
    life: Int = 0,
    invincibilityTimer: Double = 0,
    fireRateTimer: Double = 0,
    shot: Option[Vector2] = None
) extends EnemyModel
    with DynamicModel
    with FireModel
    with KeepsAway((600, 900))
    with FiresContinuously {

  type Model = MaskModel

  def withStats(stats: Stats): Model                                = copyMacro
  def withStatus(status: EnemyState): Model                         = copyMacro
  def withAlive(life: Int, invincibilityTimer: Double): Model       = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model  = copyMacro
  def withFire(fireRateTimer: Double, shot: Option[Vector2]): Model = copyMacro
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
    life = MaxLife @@ Stats.Isaac
  )
}
