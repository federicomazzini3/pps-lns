package lns.scenes.game.shot

import indigo.*
import indigo.shared.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.*
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.*

import scala.language.implicitConversions

given Conversion[Vector2, Vertex] with
  def apply(v: Vector2): Vertex = Vertex(v.x, v.y)

/**
 * Shot model that is alive, it's dynamic by computing its speed by initial direction and maxSpeed, and can damage other
 * objects
 * @param boundingBox
 *   [[AnythingModel]] boundingBox
 * @param speed
 *   [[DynamicModel]] speed
 * @param maxSpeed
 *   maxSpeed to compute new speed
 * @param direction
 *   direction to compute new speed
 * @param damage
 *   [[DamageModel]] damage
 * @param range
 *   to check the max distance shot and interrupt his movement
 * @param life
 *   [[AliveModel]] life, default 1
 * @param invincibilityTimer
 *   [[AliveModel]] invincibilityTimer, default 0
 */
case class ShotModel(
    id: AnythingId,
    view: () => ShotView[_],
    boundingBox: BoundingBox,
    owner: AnythingId,
    shotAreaOffset: Int,
    stats: Stats,
    direction: Vector2,
    range: Double,
    speed: Vector2 = Vector2(0, 0),
    life: Double = 1,
    invincibilityTimer: Double = 0,
    crossable: Boolean = false
) extends AliveModel
    with DynamicModel
    with DamageModel
    with SolidModel {

  type Model = ShotModel

  def withAlive(life: Double, invincibilityTimer: Double): Model   = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro
  def withStats(stats: Stats): Model                               = copyMacro
  def withRange(range: Double): Model                              = copyMacro
  def withSolid(crossable: Boolean): Model                         = copyMacro

  def computeSpeed(context: FrameContext[StartupData])(gameContext: GameContext): Vector2 = {
    val maxSpeed = MaxSpeed @@ stats * context.gameTime.delta.toDouble
    val speed = range match {
      case range if range - maxSpeed >= 0 => MaxSpeed @@ stats
      case range if range > 1             => range / context.gameTime.delta.toDouble
      case _                              => 0
    }
    direction.normalise * speed
  }

  def rangeAvailable(range: Double, speed: Vector2, context: FrameContext[StartupData]): Double =
    range - (speed.abs.length * context.gameTime.delta.toDouble)

  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    for {
      superObj <- super.update(context)(gameContext)
      newObj = life match {
        case life if life > 0 && range <= 0 => superObj.withAlive(0, 0)
        case _                              => superObj
      }
      retObj = newObj.speed match {
        case Vector2.zero if range > 0 => newObj.withRange(0).asInstanceOf[Model]
        case Vector2.zero              => newObj
        case _ if newObj.life > 0 =>
          newObj.withRange(rangeAvailable(range, newObj.speed, context)).asInstanceOf[Model]
        case _ => newObj
      }
    } yield retObj
}

/**
 * Extends [[GlobalEvent]] and can be intercepted by the [[GameView]] to create a new [[ShotModel]]
 */
case class ShotEvent(shot: ShotModel) extends GlobalEvent

/**
 * Factory of [[ShotModel]]
 */
object ShotModel {
  def apply(owner: AnythingId, position: Vector2, direction: Vector2, stats: Stats): ShotModel = ShotModel(
    AnythingId.generate,
    view = () => ShotView,
    BoundingBox(
      position,
      Vertex(40, 40)
    ),
    owner,
    shotAreaOffset = 0,
    stats,
    direction,
    Range @@ stats
  )
}
