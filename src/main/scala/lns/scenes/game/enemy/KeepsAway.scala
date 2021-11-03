package lns.scenes.game.enemy

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingModel, DynamicModel }
import lns.scenes.game.room.RoomModel

/**
 * Follower pure mixin that define a computeSpeed mode for an enemy that keeps away from the character at certain
 * distance
 * @param maxSpeed
 *   speed of the enemy
 * @param range
 *   Tuple to specify the min and max distance from the character to keep
 */
trait KeepsAway(maxSpeed: Int, range: (Int, Int)) { this: DynamicModel =>
  def computeSpeed(context: FrameContext[StartupData])(room: RoomModel)(character: AnythingModel): Vector2 =
    getPosition().distanceTo(character.getPosition()) match {
      case x if x < range._1 => (getPosition() - character.getPosition()).normalise * maxSpeed
      case x if x > range._2 => (character.getPosition() - getPosition()).normalise * maxSpeed
      case _                 => Vector2.zero
    }

}
