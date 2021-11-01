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
  type Model <: AnythingModel

  /**
   * Update request called during game loop on every frame
   * @param context
   *   indigo frame context data
   * @param room
   *   current room in which the Anything is placed
   * @return
   *   the Outcome of the updated viewModel
   */
  def update(context: FrameContext[StartupData], model: Model): Outcome[ViewModel] = Outcome(this)
}

trait FireViewModel extends AnythingViewModel {
  type ViewModel >: this.type <: FireViewModel
  type Model <: FireModel

  val fireState: FireState
  val fireAnimationTimer: Double

  def withFireTimer(fireAnimationTimer: Double, fireState: FireState): ViewModel

  override def update(context: FrameContext[StartupData], model: Model): Outcome[ViewModel] =
    for {
      superObj <- super.update(context, model)
      newObj = fireAnimationTimer match {
        case 0 if model.isFiring() =>
          superObj.withFireTimer(model.fireRate, model.getFireState()).asInstanceOf[ViewModel]
        case 0 => superObj
        case _ if fireAnimationTimer - context.gameTime.delta.toDouble > 0 =>
          superObj
            .withFireTimer(fireAnimationTimer - context.gameTime.delta.toDouble, fireState)
            .asInstanceOf[ViewModel]
        case _ => superObj.withFireTimer(0, FireState.NO_FIRE).asInstanceOf[ViewModel]
      }
    } yield newObj
}
