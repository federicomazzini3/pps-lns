package lns.scenes.game.enemies

import scala.language.implicitConversions

import indigo.*
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ AnythingModel, DynamicModel, given }
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

  protected def computeSpeed(context: FrameContext[StartupData])(gameContext: GameContext): Vector2 =
    status.head match {
      case (EnemyState.Attacking | EnemyState.Defending, _, _) if path.nonEmpty =>
        (path.head - getPosition()).normalise * MaxSpeed @@ stats
      case _ => Vector2.zero
    }

  override protected def limitMove(move: Vector2): Vector2 =
    val distance = path.head.distanceTo(getPosition());
    distance < move.length match {
      case true => move.normalise * distance
      case _    => move
    }

  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    for {
      superObj <- super.update(context)(gameContext)
      newObj = status.head match {
        case (EnemyState.Attacking | EnemyState.Defending, _, _)
            if path.nonEmpty &&
              (getPosition().distanceTo(path.head) < 1.0 || collisionDetected || superObj.movedBy(this) == 0) =>
          superObj.withTraveller(path.dequeue._2).asInstanceOf[Model]
        case _ => superObj
      }
    } yield newObj
}
