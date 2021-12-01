package lns.scenes.game.enemies

import indigo.*
import indigo.shared.*
import lns.StartupData
import lns.scenes.game.anything.{ AnythingViewModel, Timer }

/**
 * Base viewModel for enemy. It is designed to be extended or mixed with other [[AnythingViewModel]] traits.
 */
trait EnemyViewModel[M <: EnemyModel] extends AnythingViewModel[M] {
  type ViewModel >: this.type <: EnemyViewModel[M]

  val lastState: EnemyState
  val animationTimer: Timer

  def withLastState(lastState: EnemyState, animationTimer: Timer): ViewModel

  /**
   * Update request called during game loop on every frame.
   * @param context
   *   indigo frame context data
   * @param model
   *   the model of the Anything to which the viewModel is bound
   * @return
   *   the Outcome of the updated model
   */
  override def update(context: FrameContext[StartupData])(model: Model): Outcome[ViewModel] =
    super.update(context)(model)
}
