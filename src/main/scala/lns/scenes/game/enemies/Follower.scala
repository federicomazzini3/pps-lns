package lns.scenes.game.enemies

import indigo.*
import lns.StartupData
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ AnythingModel, DynamicModel }
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

/**
 * Follower pure mixin that define a computeSpeed mode that follows the character
 * @param maxSpeed
 *   speed of the enemy
 */
trait Follower { this: EnemyModel with DynamicModel =>
  def computeSpeed(context: FrameContext[StartupData])(gameContext: GameContext): Vector2 =
    status.head match {
      case (EnemyState.Attacking, _, _) =>
        (gameContext.character.getPosition() - getPosition()).normalise * MaxSpeed @@ stats
      case _ => Vector2.zero
    }
}
