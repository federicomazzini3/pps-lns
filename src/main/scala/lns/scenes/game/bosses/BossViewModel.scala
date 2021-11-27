package lns.scenes.game.bosses

import indigo.*
import lns.StartupData
import lns.core.Animations.*
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.{ AnythingId, AnythingViewModel, Timer, elapsed }
import lns.scenes.game.enemies.{ EnemyState, EnemyViewModel }

/**
 * Boss ViewModel to manage its animations
 */

case class BossViewModel(
    id: AnythingId,
    lastState: EnemyState = EnemyState.Idle,
    animationTimer: Timer = 0
) extends EnemyViewModel[BossModel] {
  type ViewModel = BossViewModel

  def withLastState(lastState: EnemyState, animationTimer: Timer): ViewModel = copyMacro

  override def update(context: FrameContext[StartupData])(model: Model): Outcome[ViewModel] =
    for {
      superObj <- super.update(context)(model)
      newObj = model.status.head match {
        case state @ (EnemyState.Hiding, time, _) if lastState != state._1 =>
          superObj.withLastState(state._1, time).asInstanceOf[ViewModel]
        case state @ (EnemyState.Falling, time, _) if lastState != state._1 =>
          superObj.withLastState(state._1, time).asInstanceOf[ViewModel]
        case state @ ((EnemyState.Hiding, _, _) | (EnemyState.Falling, _, _)) if animationTimer > 0 =>
          superObj
            .withLastState(state._1, animationTimer.elapsed(context.gameTime.delta.toDouble))
            .asInstanceOf[ViewModel]
        case state if lastState != state._1 => superObj.withLastState(state._1, 0).asInstanceOf[ViewModel]
        case _                              => superObj
      }
    } yield newObj
}

object BossViewModel {
  def initial(id: AnythingId): BossViewModel = BossViewModel(id)
}
