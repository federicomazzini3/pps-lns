package lns.scenes.game.room

import indigo.shared.{ FrameContext, Outcome }
import lns.StartupData
import lns.scenes.game.anything.{ AnythingId, Timer, AnythingViewModel, given }
import lns.scenes.game.enemies.parabite.{ ParabiteModel, ParabiteViewModel }
import lns.scenes.game.dungeon.*

import scala.language.implicitConversions

case class RoomViewModel(val positionInDungeon: Position, val anythings: Map[AnythingId, AnythingViewModel[_]]) {

  /**
   * Call the method update in all of anythings in a room. Can be override from subclasses for more specific behavior
   * @param context
   * @return
   *   a new updated set of anything model
   */
  def updateAnythings(
      context: FrameContext[StartupData],
      model: RoomModel
  ): Outcome[Map[AnythingId, AnythingViewModel[_]]] =
    anythings.map((id, anyViewModel) =>
      model.anythings
        .get(id)
        .fold(id -> Outcome(anyViewModel))(anyModel => id -> anyViewModel.update(context)(anyModel))
    )

  def update(context: FrameContext[StartupData], model: RoomModel): Outcome[RoomViewModel] =
    val out = updateAnythings(context, model)
    out.map(anythings => copy(anythings = anythings))

}

object RoomViewModel {
  def initial(model: RoomModel): RoomViewModel =
    RoomViewModel(
      model.positionInDungeon,
      model.anythings.map((id, any) => (id -> any.view().viewModel(id))).collect {
        case (id, any: AnythingViewModel[_]) => id -> any
      }
    )
}
