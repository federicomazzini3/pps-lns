package lns.scenes.game.shot

import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ *, given }
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

import scala.language.implicitConversions

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
    boundingBox: BoundingBox,
    owner: AnythingId,
    shotAreaOffset: Int,
    stats: Stats,
    direction: Vector2,
    speed: Vector2 = Vector2(0, 0),
    life: Double = 1,
    invincibilityTimer: Double = 0
) extends AliveModel
    with DynamicModel
    with DamageModel
    with SolidModel {

  type Model = ShotModel

  val crossable = false

  def withAlive(life: Double, invincibilityTimer: Double): Model   = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro
  def withStats(stats: Stats): Model                               = copyMacro

  def computeSpeed(context: FrameContext[StartupData])(gameContext: GameContext): Vector2 =
    direction.normalise * MaxSpeed @@ stats
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
    BoundingBox(
      position,
      Vertex(40, 40)
    ),
    owner,
    shotAreaOffset = 0,
    stats,
    direction
  )
}
