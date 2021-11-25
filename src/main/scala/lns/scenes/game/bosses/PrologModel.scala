package lns.scenes.game.bosses

import indigo.*
import indigo.shared.*
import lns.StartupData
import lns.core.PrologClient
import lns.scenes.game.GameContext
import lns.scenes.game.enemies.*
import lns.subsystems.prolog.Substitution

/**
 * Prolog mixin for AnythingModel to manage artificial intelligence behaviors in prolog
 */
trait PrologModel(name: String) extends EnemyModel {
  type Model >: this.type <: PrologModel

  val prologClient: PrologClient

  def withProlog(prologClient: PrologClient): Model

  def goal(context: FrameContext[StartupData])(gameContext: GameContext): String

  def behaviour(response: Substitution): Outcome[Model] =
    println("RESPONSE")
    println(response)
    Outcome(this.withStatus((EnemyState.Attacking, 5) +: status.drop(1)))

  def consult(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    prologClient
      .consult(
        context.startUpData.getPrologFile(name).get,
        goal(context)(gameContext)
      )
      .map(pi => withProlog(pi))

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
