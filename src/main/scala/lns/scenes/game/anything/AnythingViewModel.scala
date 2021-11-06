package lns.scenes.game.anything

import scala.language.implicitConversions
import indigo.*
import indigo.shared.*
import lns.StartupData
import lns.scenes.game.room.RoomModel
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.*

import java.util.UUID

given vmInverionsSet: Conversion[Set[Outcome[AnythingViewModel]], Outcome[Set[AnythingViewModel]]] with
  def apply(set: Set[Outcome[AnythingViewModel]]): Outcome[Set[AnythingViewModel]] =
    set.foldLeft(Outcome(Set[AnythingViewModel]().empty))((acc, el) => acc.merge(el)((set, el) => set + el))

given vmInversionMap: Conversion[Map[UUID, Outcome[AnythingViewModel]], Outcome[Map[UUID, AnythingViewModel]]] with
  def apply(set: Map[UUID, Outcome[AnythingViewModel]]): Outcome[Map[UUID, AnythingViewModel]] =
    set.foldLeft(Outcome(Map[UUID, AnythingViewModel]().empty))((acc, el) =>
      acc.merge(el._2)((set, el2) => set + (el._1 -> el2))
    )

/**
 * Base viewModel for every thing placed inside a room
 */
trait AnythingViewModel {
  type ViewModel >: this.type <: AnythingViewModel
  type Model <: AnythingModel

  /**
   * Update request called during game loop on every frame
   * @param context
   *   indigo frame context data
   * @param room
   *   current room in which the Anything is placed
   * @return
   *   the Outcome of the updated viewModel
   */
  def update(context: FrameContext[StartupData], model: Model): Outcome[ViewModel] = Outcome(this)
}

/**
 * Base model for every thing that can fire. It is designed to be extended or mixed with other [[AnythingViewModel]]
 * traits.
 */
trait FireViewModel extends AnythingViewModel {
  type ViewModel >: this.type <: FireViewModel
  type Model <: FireModel

  val fireState: FireState
  val fireAnimationTimer: Double

  def withFireTimer(fireAnimationTimer: Double, fireState: FireState): ViewModel

  /**
   * Update request called during game loop on every frame. Check if the model referred to him firing and there isn't a
   * fireAnimationTimer to start a new animation keeping the relative fireState. Otherwise, if only the
   * fireAnimationTimer is present, it is decreased to mark the end of the animation
   * @param context
   *   indigo frame context data
   * @param room
   *   current room in which the Anything is placed
   * @return
   *   the Outcome of the updated model
   */
  override def update(context: FrameContext[StartupData], model: Model): Outcome[ViewModel] =
    for {
      superObj <- super.update(context, model)
      newObj = fireAnimationTimer match {
        case 0 if model.isFiring() =>
          superObj.withFireTimer(FireRate @@ model.stats, model.getFireState()).asInstanceOf[ViewModel]
        case 0 => superObj
        case _ if fireAnimationTimer - context.gameTime.delta.toDouble > 0 =>
          superObj
            .withFireTimer(fireAnimationTimer - context.gameTime.delta.toDouble, fireState)
            .asInstanceOf[ViewModel]
        case _ => superObj.withFireTimer(0, FireState.NO_FIRE).asInstanceOf[ViewModel]
      }
    } yield newObj
}
