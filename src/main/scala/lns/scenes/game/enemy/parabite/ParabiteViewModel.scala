package lns.scenes.game.enemy.parabite

import indigo.*
import lns.StartupData
import lns.core.Animations.*
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.{ AnythingViewModel, Timer }
import lns.scenes.game.enemy.EnemyState
import lns.scenes.game.anything.elapsed

/**
 * Parabite ViewModel to manage its animations
 */

case class ParabiteViewModel(
    lastStatus: EnemyState = EnemyState.Idle,
    animationTimer: Timer = 0
) extends AnythingViewModel {
  type ViewModel = ParabiteViewModel
  type Model     = ParabiteModel

  def withStatus(lastStatus: EnemyState, animationTimer: Timer): ViewModel = copyMacro

  override def update(context: FrameContext[StartupData], model: Model): Outcome[ViewModel] =
    for {
      superObj <- super.update(context, model)
      newObj = model.status match {
        case EnemyState.Hiding | EnemyState.Idle if lastStatus != model.status =>
          superObj.withStatus(model.status, Parabite.hideTime).asInstanceOf[ViewModel]
        case EnemyState.Hiding | EnemyState.Idle if animationTimer > 0 =>
          superObj
            .withStatus(model.status, animationTimer.elapsed(context.gameTime.delta.toDouble))
            .asInstanceOf[ViewModel]
        case _ if lastStatus != model.status => superObj.withStatus(model.status, 0).asInstanceOf[ViewModel]
        case _                               => superObj
      }
    } yield newObj
}

object ParabiteViewModel {
  def initial: ParabiteViewModel = ParabiteViewModel()
}
