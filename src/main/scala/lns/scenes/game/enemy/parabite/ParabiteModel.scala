package lns.scenes.game.enemy.parabite

import indigo.*
import indigo.shared.FrameContext
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.*
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.enemy.{ EnemyModel, EnemyState, Follower, Traveller }
import lns.scenes.game.enemy.boney.BoneyModel
import lns.scenes.game.room.{ Boundary, RoomModel }
import lns.scenes.game.shot.ShotEvent
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

import scala.collection.immutable.Queue
import scala.language.implicitConversions

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
 * @param path
 *   [[Traveller]] path, default Queue.empty
 */
case class ParabiteModel(
    boundingBox: BoundingBox,
    stats: Stats,
    status: EnemyState = EnemyState.Idle,
    speed: Vector2 = Vector2(0, 0),
    life: Int = 0,
    invincibilityTimer: Timer = 0,
    path: Queue[Vector2] = Queue.empty,
    statusTimer: Timer = 0
) extends EnemyModel
    with DynamicModel
    with Traveller {

  type Model = ParabiteModel

  def withStats(stats: Stats): Model                               = copyMacro
  def withStatus(status: EnemyState): Model                        = copyMacro
  def withAlive(life: Int, invincibilityTimer: Double): Model      = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro
  def withTraveller(path: Queue[Vector2]): Model                   = copyMacro

  override def update(context: FrameContext[StartupData])(room: RoomModel)(character: AnythingModel): Outcome[Model] =
    for {
      superObj <- super.update(context)(room)(character)
      newObj = status match {
        case EnemyState.Idle if statusTimer == 0 && getPosition().distanceTo(character.getPosition()) >= 10 =>
          superObj
            .withTraveller(Queue(character.getPosition().clamp(0, Assets.Rooms.floorSize)))
            .withStatus(EnemyState.Attacking)
            .asInstanceOf[Model] // TODO: togliere il clamp dopo refactor coordinate
        case EnemyState.Attacking if superObj.path.isEmpty == true =>
          superObj.copy(statusTimer = 2).withStatus(EnemyState.Hiding)
        case EnemyState.Hiding if statusTimer == 0 =>
          superObj.copy(statusTimer = 1).withStatus(EnemyState.Idle)
        case _ if statusTimer > 0 =>
          superObj.copy(statusTimer = statusTimer.elapsed(context.gameTime.delta.toDouble))
        case _ => superObj

      }
    } yield newObj

}

/**
 * Factory of [[ParabiteModel]]
 */
object ParabiteModel {
  def initial: ParabiteModel = ParabiteModel(
    boundingBox = BoundingBox(
      Vertex(Assets.Rooms.floorSize / 2, Assets.Rooms.floorSize / 2),
      Vertex(
        Assets.Enemies.Parabite.withScale(Assets.Enemies.Parabite.width),
        Assets.Enemies.Parabite.withScale(Assets.Enemies.Parabite.height)
      )
    ),
    stats = Stats.Isaac +++ (MaxSpeed -> 600),
    life = MaxLife @@ Stats.Isaac
  )
}
