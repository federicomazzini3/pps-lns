package lns.scenes.game.enemies

import indigo.*
import lns.StartupData
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ AnythingModel, FireModel }

/**
 * Pure mixin to specify a computeFire mode that continuously shot to the character position
 */
trait FiresContinuously { this: EnemyModel with FireModel =>
  def computeFire(context: FrameContext[StartupData])(gameContext: GameContext): Option[Vector2] = status.head match {
    case (EnemyState.Attacking, _) => Some((gameContext.character.getPosition() - getPosition()).normalise)
    case _                         => None
  }

}
