package lns.scenes.game.bosses

import indigo.*
import indigo.shared.*
import lns.StartupData
import lns.core.PrologClient
import lns.scenes.game.GameContext
import lns.scenes.game.enemies.*
import lns.subsystems.prolog.Substitution

/**
 * Prolog mixin for EnemyModel to manage artificial intelligence behaviors in prolog
 * @tparam name
 *   of prolog asset file
 */
trait PrologModel(name: String) extends EnemyModel {
  type Model >: this.type <: PrologModel

  val prologClient: PrologClient

  def withProlog(prologClient: PrologClient): Model

  /**
   * Method must to be overridden to define Prolog goal
   * @param context
   *   indigo frame context data
   * @param gameContext
   *   current [[GameContext]] containing the current room in which the Anything is placed and the character model
   * @return
   *   String: Prolog goal
   */
  def goal(context: FrameContext[StartupData])(gameContext: GameContext): String

  /**
   * Method must to be overridden to implements model behaviour
   * @param response
   *   [[Substitution]] PrologClient consult result
   * @return
   *   the Outcome of the updated model
   */
  def behaviour(response: Substitution): Outcome[Model]

  /**
   * Method to consult the prolog with the string generated by the goal method
   * @param context
   *   indigo frame context data
   * @param gameContext
   *   current [[GameContext]] containing the current room in which the Anything is placed and the character model
   * @return
   *   the Outcome of the updated model
   */
  def consult(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    prologClient
      .consult(
        context.startUpData.getPrologFile(name).get,
        goal(context)(gameContext)
      )
      .map(pi => withProlog(pi))

  /**
   * Update request called during game loop on every frame. If the EnemyModel has [[EnemyState.Idle]] is called Prolog
   * consult
   * @param context
   *   indigo frame context data
   * @param gameContext
   *   current [[GameContext]] containing the current room in which the Anything is placed and the character model
   * @return
   *   the Outcome of the updated
   */
  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    for {
      superObj <- super.update(context)(gameContext)
      newObj <- status.head match {
        case (EnemyState.Idle, _) =>
          superObj
            .withStatus((EnemyState.Consulting, -1) +: status)
            .consult(context)(gameContext)
            .asInstanceOf[Outcome[Model]]
        case _ => Outcome(superObj)
      }
    } yield newObj
}
