package lns.scenes.game.bosses

import indigo.*
import lns.StartupData
import lns.core.Animations.*
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.{ AnythingId, AnythingViewModel, Timer, elapsed }
import lns.scenes.game.enemies.EnemyState

/**
 * Boss ViewModel to manage its animations
 */

case class BossViewModel(
    id: AnythingId,
    lastState: EnemyState = EnemyState.Idle,
    animationTimer: Timer = 0
) extends AnythingViewModel[BossModel] {
  type ViewModel = BossViewModel

  def withLastState(lastState: EnemyState, animationTimer: Timer): ViewModel = copyMacro

  /*
  override def update(context: FrameContext[StartupData])(model: Model): Outcome[ViewModel] =
    for {
      superObj <- super.update(context)(model)
      newObj = model.status.head._1 match {
        case state @ (EnemyState.Hiding | EnemyState.Idle) if lastState != state =>
          superObj.withLastState(state, Loki.hideTime).asInstanceOf[ViewModel]
        case state @ (EnemyState.Hiding | EnemyState.Idle) if animationTimer > 0 =>
          superObj
            .withLastState(state, animationTimer.elapsed(context.gameTime.delta.toDouble))
            .asInstanceOf[ViewModel]
        case state if lastState != state => superObj.withLastState(state, 0).asInstanceOf[ViewModel]
        case _                           => superObj
      }
    } yield newObj
   */
}

object BossViewModel {
  def initial(id: AnythingId): BossViewModel = BossViewModel(id)
}
