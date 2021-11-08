package lns.scenes.game.enemy

import scala.language.implicitConversions

import indigo.*
import indigo.shared.FrameContext
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.scenes.game.anything.{ AnythingModel, DynamicModel, given }
import lns.scenes.game.room.RoomModel
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.*

import scala.collection.immutable.Queue

/**
 * Traveller pure mixin that define a computeSpeed mode that follows a path
 *
 * @param maxSpeed
 *   speed of the enemy
 */
trait Traveller extends DynamicModel { this: EnemyModel =>
  type Model >: this.type <: Traveller

  val path: Queue[Vector2]

  def withTraveller(path: Queue[Vector2]): Model

  def computeSpeed(context: FrameContext[StartupData])(room: RoomModel)(character: AnythingModel): Vector2 =
    status match {
      case EnemyState.Attacking | EnemyState.Defending if path.nonEmpty =>
        (path.head - getPosition()).normalise * MaxSpeed @@ stats
      case _ => Vector2.zero
    }

  override def computeMove(context: FrameContext[StartupData])(room: RoomModel)(
      character: AnythingModel
  ): (Vector2, BoundingBox) =
    val speed: Vector2 = computeSpeed(context)(room)(character) * context.gameTime.delta.toDouble
    speed.length match {
      case 0                                            => (speed, boundingBox)
      case x if path.head.distanceTo(getPosition()) < x => (speed, boundingBox.moveTo(path.head))
      case _                                            => (speed, boundingBox.moveBy(speed))
    }

  override def update(context: FrameContext[StartupData])(room: RoomModel)(character: AnythingModel): Outcome[Model] =
    for {
      superObj <- super.update(context)(room)(character)
      newObj = status match {
        case EnemyState.Attacking | EnemyState.Defending
            if path.nonEmpty && (getPosition().distanceTo(path.head) < 10) =>
          superObj.withTraveller(path.dequeue._2).asInstanceOf[Model]
        case _ => superObj
      }
    } yield newObj
}
