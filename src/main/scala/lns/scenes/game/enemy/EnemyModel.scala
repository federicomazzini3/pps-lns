package lns.scenes.game.enemy

import indigo.*
import indigo.shared.FrameContext
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.{ SolidModel, * }
import lns.scenes.game.room.RoomModel

import scala.collection.immutable.Queue
import scala.language.implicitConversions

enum EnemyState:
  case Idle, Attacking, Defending, Hiding

type EnemyStatus = (EnemyState, Timer)
extension (s1: EnemyStatus) def :+(s2: EnemyStatus): Queue[EnemyStatus] = Queue(s1, s2)

given Conversion[EnemyStatus, Queue[EnemyStatus]] with
  def apply(s: EnemyStatus): Queue[EnemyStatus] = Queue(s)

/**
 * Enemy model trait that is alive and make damage. An Enemy has a status to represent a timed queue of its intentions.
 * If there is a sequence of EnemyStatus enqued in status, then the first one will be dropped once its timer reaches
 * zero.
 */
trait EnemyModel extends AliveModel with DamageModel with SolidModel {
  type Model >: this.type <: EnemyModel

  val status: Queue[EnemyStatus]

  def withStatus(status: Queue[EnemyStatus]): Model

  override def update(context: FrameContext[StartupData])(room: RoomModel)(character: AnythingModel): Outcome[Model] =
    for {
      superObj <- super.update(context)(room)(character)
      newObj = status.head match {
        case (state, timer) if timer > 0 =>
          superObj
            .withStatus((state, timer.elapsed(context.gameTime.delta.toDouble)) +: status.drop(1))
            .asInstanceOf[Model]
        case (_, 0) if status.length > 1 => superObj.withStatus(status.drop(1)).asInstanceOf[Model]
        case _                           => superObj
      }
    } yield newObj
}
