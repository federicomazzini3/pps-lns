package lns.scenes.game.shots

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
 * @param id
 *   [[AnythingId]] The unique identifier of the Anything instance.
 * @param view
 *   [[AnythingView]] The Anything's View factory function.
 * @param boundingBox
 *   [[AnythingModel]] boundingBox
 * @param owner
 *   [[AnythingId]] The unique identifier of the Anything instance that create the ShotModel.
 * @param shotAreaOffset
 *   shotAreaOffset
 * @param stats
 *   Initial Shot [[Stats]]
 * @param direction
 *   direction to compute new speed
 * @param range
 *   to check the max distance shot and interrupt his movement
 * @param speed
 *   [[DynamicModel]] speed, defautl Vector2(0,0)
 * @param collisionDetected
 *   [[DynamicModel]] collisionDetected, true if the Anything is collided with some other Anything. Default false
 * @param life
 *   [[AliveModel]] life, default 1
 * @param invincibilityTimer
 *   [[AliveModel]] invincibilityTimer, default 0
 * @param crossable
 *   crossable, default false
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
    collisionDetected: Boolean = false,
    life: Double = 1,
    invincibilityTimer: Double = 0,
    crossable: Boolean = false
) extends AliveModel
    with DynamicModel
    with DamageModel
    with SolidModel {

  type Model = ShotModel

  def withAlive(life: Double, invincibilityTimer: Double): Model                               = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2, collisionDetected: Boolean): Model = copyMacro
  def withStats(stats: Stats): Model                                                           = copyMacro
  def withRange(range: Double): Model                                                          = copyMacro
  def withSolid(crossable: Boolean): Model                                                     = copyMacro

  protected def computeSpeed(context: FrameContext[StartupData])(gameContext: GameContext): Vector2 = range match {
    case 0 => Vector2.zero
    case _ => direction.normalise * MaxSpeed @@ stats
  }

  override protected def limitMove(move: Vector2): Vector2 = range < move.length match {
    case true => move.normalise * range
    case _    => move
  }

  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    for {
      superObj <- super.update(context)(gameContext)
      retObj = (superObj.movedBy(this), range == 0 || collisionDetected) match {
        case (_, true)                      => superObj.withAlive(0, 0).asInstanceOf[Model]
        case (x, _) if x == 0 || range <= x => superObj.withRange(0).asInstanceOf[Model]
        case (x, _)                         => superObj.withRange(range - x).asInstanceOf[Model]
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
  def apply(owner: AnythingId, position: Vector2, direction: Vector2, stats: Stats)(
      view: () => ShotView[_]
  ): ShotModel = ShotModel(
    AnythingId.generate,
    view = view,
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
