package lns.scenes.game.anything

import indigo.*
import indigo.shared.*
import lns.StartupData
import lns.scenes.game.room.RoomModel
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.*

import scala.language.implicitConversions
import scala.annotation.targetName
import scala.reflect.*

given vmInverionsSet: Conversion[Set[Outcome[AnythingViewModel[_]]], Outcome[Set[AnythingViewModel[_]]]] with
  def apply(set: Set[Outcome[AnythingViewModel[_]]]): Outcome[Set[AnythingViewModel[_]]] =
    set.foldLeft(Outcome(Set[AnythingViewModel[_]]().empty))((acc, el) => acc.merge(el)((set, el) => set + el))

given vmInversionMap
    : Conversion[Map[AnythingId, Outcome[AnythingViewModel[_]]], Outcome[Map[AnythingId, AnythingViewModel[_]]]] with
  def apply(set: Map[AnythingId, Outcome[AnythingViewModel[_]]]): Outcome[Map[AnythingId, AnythingViewModel[_]]] =
    set.foldLeft(Outcome(Map[AnythingId, AnythingViewModel[_]]().empty))((acc, el) =>
      acc.merge[AnythingViewModel[_], Map[AnythingId, AnythingViewModel[_]]](el._2)((set, el2) => set + (el._1 -> el2))
    )

/**
 * Base viewModel for every thing placed inside a room
 */
trait AnythingViewModel[M <: AnythingModel: Typeable] {
  type ViewModel >: this.type <: AnythingViewModel[M]
  type Model = M

  /**
   * The unique identifier of the Anything instance. Need to match the [[AnythingModel]] id.
   */
  val id: AnythingId

  /**
   * Update request called during game loop on every frame
   * @param context
   *   indigo frame context data
   * @param model
   *   the model of the Anything to which the viewModel is bound
   * @return
   *   the Outcome of the updated viewModel
   */
  def update(context: FrameContext[StartupData])(model: Model): Outcome[ViewModel] = Outcome(this)

  /**
   * Generic update that accepts every [[AnythingModel]] and checks for type correctness at runtime. Useful when you
   * need to update the viewModel using an [[AnythingModel]] that you don't know its type. If the type is incorrect then
   * the update is skipped
   * @param contex
   *   Indigo frame context data
   * @param model
   *   the model of the Anything to which the viewModel is bound, its type maybe not correct.
   * @return
   *   the Outcome of the updated viewModel or of the same viewModel if type check fails
   */
  @targetName("anyUpdate")
  def update(contex: FrameContext[StartupData])(model: AnythingModel): Outcome[ViewModel] =
    model match {
      case m: Model => update(contex)(m)
      case _        => Outcome(this)
    }
}

/**
 * Base viewModel for every thing that can fire. It is designed to be extended or mixed with other [[AnythingViewModel]]
 * traits.
 */
trait FireViewModel[M <: FireModel] extends AnythingViewModel[M] {
  type ViewModel >: this.type <: FireViewModel[M]

  val fireState: FireState
  val fireAnimationTimer: Timer

  def withFireTimer(fireAnimationTimer: Timer, fireState: FireState): ViewModel

  /**
   * Update request called during game loop on every frame. Check if the model referred to him firing and there isn't a
   * fireAnimationTimer to start a new animation keeping the relative fireState. Otherwise, if only the
   * fireAnimationTimer is present, it is decreased to mark the end of the animation
   * @param context
   *   indigo frame context data
   * @param model
   *   the model of the Anything to which the viewModel is bound
   * @return
   *   the Outcome of the updated model
   */
  override def update(context: FrameContext[StartupData])(model: Model): Outcome[ViewModel] =
    for {
      superObj <- super.update(context)(model)
      newTimer = fireAnimationTimer.elapsed(context.gameTime.delta.toDouble)
      newObj = newTimer match {
        case 0 if model.isFiring() =>
          superObj.withFireTimer(FireRate @@ model.stats, model.getFireState()).asInstanceOf[ViewModel]
        case 0 =>
          superObj.withFireTimer(0, FireState.NO_FIRE).asInstanceOf[ViewModel]
        case _ =>
          superObj.withFireTimer(newTimer, fireState).asInstanceOf[ViewModel]
      }
    } yield newObj
}
