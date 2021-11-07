package lns.scenes.game.room

import indigo.shared.{ FrameContext, Outcome }
import lns.StartupData
import lns.scenes.game.anything.AnythingViewModel
import lns.scenes.game.anything.given
import lns.scenes.game.anything.vmInversionMap
import lns.scenes.game.anything.vmInverionsSet
import lns.scenes.game.enemy.parabite.{ ParabiteModel, ParabiteViewModel }

import java.util.UUID
import scala.language.implicitConversions

case class RoomViewModel(val positionInDungeon: Position, val anythings: Map[UUID, AnythingViewModel]) {

  /**
   * Call the method update in all of anythings in a room. Can be override from subclasses for more specific behavior
   * @param context
   * @return
   *   a new updated set of anything model
   */
  def updateAnythings(context: FrameContext[StartupData], model: RoomModel): Outcome[Map[UUID, AnythingViewModel]] =
    anythings
      //.map((id, any) => id -> any.update(context, model.anythings(id)))
      .map((id, any) =>
        (id -> (any match {
          case any: ParabiteViewModel =>
            any.update(context, model.anythings(id).asInstanceOf[ParabiteModel]) //TODO: RISOLVERE!!!
        }))
      )

  def update(context: FrameContext[StartupData], model: RoomModel): Outcome[RoomViewModel] =
    val out = updateAnythings(context, model)
    out.map(anythings => copy(anythings = anythings))

}

object RoomViewModel {
  def initial(model: RoomModel): RoomViewModel =
    RoomViewModel(
      model.positionInDungeon,
      model.anythings
        .collect { case (id, e: ParabiteModel): (UUID, ParabiteModel) => (id -> e) }
        .map((id, any) =>
          (id -> (any match {
            case a: ParabiteModel => println("VIEW MODEL CREATO"); ParabiteViewModel()
          }))
        )
    )

}
