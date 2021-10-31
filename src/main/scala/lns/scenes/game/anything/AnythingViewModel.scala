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
  def update(context: FrameContext[StartupData]): Outcome[ViewModel] = Outcome(this)
}

trait FireViewModel extends AnythingViewModel {
  type ViewModel >: this.type <: FireViewModel

  val fireState: FireState
  val fireAnimationTimer: Double

  def withFireTimer(fireAnimationTimer: Double, fireState: FireState): ViewModel

  def fire(context: FrameContext[StartupData], model: FireModel): Outcome[ViewModel] = fireAnimationTimer match {
    case 0 => Outcome(withFireTimer(model.fireRate, model.getFireState()))
    case _ => Outcome(this)
  }

  override def update(context: FrameContext[StartupData]): Outcome[ViewModel] =
    println
    for {
      superObj <- super.update(context)
      newObj = fireAnimationTimer match {
        case 0 => superObj
        case _ if fireAnimationTimer - context.gameTime.delta.toDouble > 0 =>
          superObj
            .withFireTimer(fireAnimationTimer - context.gameTime.delta.toDouble, fireState)
            .asInstanceOf[ViewModel]
        case _ => superObj.withFireTimer(0, FireState.NO_FIRE).asInstanceOf[ViewModel]
      }
    } yield newObj
}
