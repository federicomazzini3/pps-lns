package lns.scenes.game.enemies

import indigo.*
import lns.StartupData
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ AnythingModel, DynamicModel }
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

/**
 * Follower pure mixin that define a computeSpeed mode for an enemy that keeps away from the character at certain
 * distance
 * @param range
 *   Tuple to specify the min and max distance from the character to keep
 */
trait KeepsAway { this: EnemyModel with DynamicModel =>
  protected def computeSpeed(context: FrameContext[StartupData])(gameContext: GameContext): Vector2 =
    status.head match {
      case (EnemyState.Idle, _, _) => Vector2.zero
      case _ =>
        getPosition().distanceTo(gameContext.character.getPosition()) match {
          case x if x < KeepAwayMin @@ stats =>
            (getPosition() - gameContext.character.getPosition()).normalise * MaxSpeed @@ stats
          case x if x > KeepAwayMax @@ stats =>
            (gameContext.character.getPosition() - getPosition()).normalise * MaxSpeed @@ stats
          case _ => Vector2.zero
        }
    }
}
