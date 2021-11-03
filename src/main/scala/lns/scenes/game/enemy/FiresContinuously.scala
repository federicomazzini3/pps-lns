package lns.scenes.game.enemy

import indigo.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingModel, FireModel }
import lns.scenes.game.room.RoomModel

/**
 * Pure mixin to specify a computeFire mode that continuously shot to the character position
 */
trait FiresContinuously { this: FireModel =>
  def computeFire(context: FrameContext[StartupData])(character: AnythingModel): Option[Vector2] =
    Some((character.getPosition() - getPosition()).normalise)
}
