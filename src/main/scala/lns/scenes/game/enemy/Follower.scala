package lns.scenes.game.enemy

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingModel, DynamicModel }
import lns.scenes.game.room.RoomModel
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

/**
 * Follower pure mixin that define a computeSpeed mode that follows the character
 * @param maxSpeed
 *   speed of the enemy
 */
trait Follower { this: EnemyModel with DynamicModel =>
  def computeSpeed(context: FrameContext[StartupData])(room: RoomModel)(character: AnythingModel): Vector2 =
    status.head match {
      case (EnemyState.Attacking, _) => (character.getPosition() - getPosition()).normalise * MaxSpeed @@ stats
      case _                         => Vector2.zero
    }
}
