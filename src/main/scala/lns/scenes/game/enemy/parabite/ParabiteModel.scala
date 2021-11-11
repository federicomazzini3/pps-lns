package lns.scenes.game.enemy.parabite

import indigo.*
import indigo.shared.FrameContext
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ *, given }
import lns.scenes.game.enemy.{ *, given }
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
    id: AnythingId,
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Idle, 0)),
    val crossable: Boolean = false,
    speed: Vector2 = Vector2(0, 0),
    life: Double = 0,
    invincibilityTimer: Timer = 0,
    path: Queue[Vector2] = Queue.empty
) extends EnemyModel
    with DynamicModel
    with Traveller {

  type Model = ParabiteModel

  def withStats(stats: Stats): Model                               = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model                = copyMacro
  def withAlive(life: Double, invincibilityTimer: Double): Model   = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro
  def withTraveller(path: Queue[Vector2]): Model                   = copyMacro

  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    for {
      superObj <- super.update(context)(gameContext)
      newObj = status.head match {
        case (EnemyState.Idle, 0) if getPosition().distanceTo(gameContext.character.getPosition()) >= 10 =>
          superObj
            .withTraveller(Queue(gameContext.character.getPosition().clamp(0, Assets.Rooms.floorSize)))
            .withStatus((EnemyState.Attacking, 0.0))
            .asInstanceOf[Model] // TODO: togliere il clamp dopo refactor coordinate
        case (EnemyState.Attacking, _) if superObj.path.isEmpty == true =>
          superObj.withStatus((EnemyState.Hiding, 2.0) :+ (EnemyState.Idle, 1.0)).asInstanceOf[Model]
        case _ => superObj

      }
    } yield newObj

}

/**
 * Factory of [[ParabiteModel]]
 */
object ParabiteModel {
  import Assets.Enemies.Parabite.*
  def initial: ParabiteModel = ParabiteModel(
    AnythingId.generate,
    boundingBox = BoundingBox(
      Vector2(Assets.Rooms.floorSize / 2, Assets.Rooms.floorSize / 2),
      Vector2(
        withScale(width),
        withScale(height - offsetY)
      )
    ),
    shotAreaOffset = withScale(-offsetY),
    stats = Stats.Parabite,
    life = MaxLife @@ Stats.Parabite
  )
}
