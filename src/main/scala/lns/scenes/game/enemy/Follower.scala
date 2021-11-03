package lns.scenes.game.enemy

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingModel, DynamicModel }
import lns.scenes.game.room.RoomModel

/**
 * Follower pure mixin that define a computeSpeed mode that follows the character
 * @param maxSpeed
 *   speed of the enemy
 */
trait Follower(maxSpeed: Int) { this: DynamicModel =>
  def computeSpeed(context: FrameContext[StartupData])(room: RoomModel)(character: AnythingModel): Vector2 =
    (character.getPosition() - getPosition()).normalise * maxSpeed
}
