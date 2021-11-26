package lns.scenes.game.enemies

import indigo.*
import indigo.shared.FrameContext
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.*
import lns.scenes.game.enemies.boney.BoneyModel
import lns.scenes.game.enemies.mask.MaskModel
import lns.scenes.game.enemies.nerve.NerveModel
import lns.scenes.game.enemies.parabite.ParabiteModel
import lns.scenes.game.room.{ Cell, Floor, RoomModel }
import lns.scenes.game.enemies.{ boney, mask, nerve, parabite }

import scala.collection.immutable.Queue
import scala.language.implicitConversions
import scala.util.Random

enum EnemyState:
  case Idle, Attacking, Defending, Hiding, Falling, Consulting

type EnemyStatus = (EnemyState, Timer, Option[Any])
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

  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    for {
      superObj <- super.update(context)(gameContext)
      newObj = status.head match {
        case (state, timer, opt) if timer > 0 =>
          superObj
            .withStatus((state, timer.elapsed(context.gameTime.delta.toDouble), opt) +: status.drop(1))
            .asInstanceOf[Model]
        case (_, 0, _) if status.length > 1 => superObj.withStatus(status.drop(1)).asInstanceOf[Model]
        case _                              => superObj
      }
    } yield newObj
}

object EnemyModel {
  val enemyModels = Seq(
    NerveModel.initial,
    MaskModel.initial,
    ParabiteModel.initial,
    BoneyModel.initial
  )

  def random(cells: Seq[Cell]): Map[AnythingId, AnythingModel] =

    def _random(cells: Seq[Cell], enemies: Map[AnythingId, AnythingModel]): Map[AnythingId, AnythingModel] =
      if cells.size <= 0 then enemies
      else {
        val enemy = Random.shuffle(enemyModels).head(cells.head)
        _random(cells.tail, enemies + (enemy.id -> enemy))
      }

    val enemiesNumber =
      cells.size match {
        case n if n > 8 => Random.between(1, 7)
        case n          => Random.between(1, n)
      }

    val cellsWithoutDoor = cells.filter(cell => cell.x != Floor.nCenter / 2 && cell.y != Floor.nCenter / 2)

    _random(Random.shuffle(cellsWithoutDoor).take(enemiesNumber), Map.empty)
}
