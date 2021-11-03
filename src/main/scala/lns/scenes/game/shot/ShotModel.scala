package lns.scenes.game.shot

import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.{ *, given }
import lns.scenes.game.room.RoomModel
import org.scalajs.dom.raw.Position

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
    speed: Vector2,
    maxSpeed: Vector2,
    direction: Vector2,
    damage: Double,
    range: Double,
    life: Int = 1,
    invincibilityTimer: Double = 0
) extends AliveModel
    with DynamicModel
    with DamageModel {

  type Model = ShotModel
  val invincibility: Double = 0

  def withAlive(life: Int, invincibilityTimer: Double): Model      = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro
  def withDamage(damage: Double): Model                            = copyMacro

  def computeSpeed(context: FrameContext[StartupData])(room: RoomModel)(character: AnythingModel): Vector2 =
    maxSpeed * direction
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
      Vertex(5, 5)
    ),
    Vector2(0, 0),
    Vector2(800, 800),
    direction,
    10,
    500
  )
}
