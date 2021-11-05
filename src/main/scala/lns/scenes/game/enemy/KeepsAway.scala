package lns.scenes.game.enemy

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingModel, DynamicModel }
import lns.scenes.game.room.RoomModel
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

/**
 * Follower pure mixin that define a computeSpeed mode for an enemy that keeps away from the character at certain
 * distance
 * @param range
 *   Tuple to specify the min and max distance from the character to keep
 */
trait KeepsAway(range: (Int, Int)) { this: EnemyModel with DynamicModel =>
  def computeSpeed(context: FrameContext[StartupData])(room: RoomModel)(character: AnythingModel): Vector2 =
    status match {
      case EnemyState.Idle => Vector2.zero
      case _ =>
        getPosition().distanceTo(character.getPosition()) match {
          case x if x < range._1 => (getPosition() - character.getPosition()).normalise * MaxSpeed @@ stats
          case x if x > range._2 => (character.getPosition() - getPosition()).normalise * MaxSpeed @@ stats
          case _                 => Vector2.zero
        }
    }
}
