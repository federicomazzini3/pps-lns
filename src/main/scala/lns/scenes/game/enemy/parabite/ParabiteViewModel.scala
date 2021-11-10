package lns.scenes.game.enemy.parabite

import indigo.*
import lns.StartupData
import lns.core.Animations.*
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.{ AnythingId, Timer, AnythingViewModel }
import lns.scenes.game.enemy.EnemyState
import lns.scenes.game.anything.elapsed

/**
 * Parabite ViewModel to manage its animations
 */

case class ParabiteViewModel(
    id: AnythingId,
    lastState: EnemyState = EnemyState.Idle,
    animationTimer: Timer = 0
) extends AnythingViewModel {
  type ViewModel = ParabiteViewModel
  type Model     = ParabiteModel

  def withLastState(lastState: EnemyState, animationTimer: Timer): ViewModel = copyMacro

  override def update(context: FrameContext[StartupData], model: Model): Outcome[ViewModel] =
    for {
      superObj <- super.update(context, model)
      newObj = model.status.head._1 match {
        case state @ (EnemyState.Hiding | EnemyState.Idle) if lastState != state =>
          superObj.withLastState(state, Parabite.hideTime).asInstanceOf[ViewModel]
        case state @ (EnemyState.Hiding | EnemyState.Idle) if animationTimer > 0 =>
          superObj
            .withLastState(state, animationTimer.elapsed(context.gameTime.delta.toDouble))
            .asInstanceOf[ViewModel]
        case state if lastState != state => superObj.withLastState(state, 0).asInstanceOf[ViewModel]
        case _                           => superObj
      }
    } yield newObj
}

object ParabiteViewModel {
  def initial(model: ParabiteModel): ParabiteViewModel = ParabiteViewModel(model.id)
}
