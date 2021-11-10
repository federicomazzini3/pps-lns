package lns.scenes.game.shot

import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Macros.copyMacro
import org.scalajs.dom.raw.Position
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ SolidModel, given_Conversion_Vector2_Vertex, * }
import lns.scenes.game.stats.*
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
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    direction: Vector2,
    speed: Vector2 = Vector2(0, 0),
    life: Int = 1,
    invincibilityTimer: Double = 0
) extends AliveModel
    with DynamicModel
    with DamageModel
    with SolidModel {

  type Model = ShotModel

  val crossable = false

  def withAlive(life: Int, invincibilityTimer: Double): Model      = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro
  def withStats(stats: Stats): Model                               = copyMacro

  def computeSpeed(context: FrameContext[StartupData])(gameContext: GameContext): Vector2 =
    direction.normalise * MaxSpeed @@ stats
}

/**
 * Extends [[GlobalEvent]] and can be intercepted by the [[GameView]] to create a new [[ShotModel]]
 */
case class ShotEvent(position: Vector2, direction: Vector2) extends GlobalEvent

/**
 * Factory of [[ShotModel]]
 */
object ShotModel {
  def apply(position: Vector2, direction: Vector2): ShotModel = ShotModel(
    BoundingBox(
      position,
      Vertex(40, 40)
    ),
    shotAreaOffset = 0,
    Stats.Shot,
    direction
  )
}
