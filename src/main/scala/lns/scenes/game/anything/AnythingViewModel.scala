package lns.scenes.game.anything

import indigo.*
import indigo.shared.*
import lns.StartupData
import lns.scenes.game.room.RoomModel

/**
 * Base viewModel for every thing placed inside a room
 */
trait AnythingViewModel {
  type ViewModel >: this.type <: AnythingViewModel

  /**
   * Update request called during game loop on every frame
   * @param context
   *   indigo frame context data
   * @param room
   *   current room in which the Anything is placed
   * @return
   *   the Outcome of the updated viewModel
   */
  def update(context: FrameContext[StartupData])(room: RoomModel): Outcome[ViewModel] = Outcome(this)
}
