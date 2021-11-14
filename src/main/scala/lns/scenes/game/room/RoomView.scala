package lns.scenes.game.room

import indigo.shared.FrameContext
import indigo.shared.datatypes.{ Rectangle, Vector2 }
import indigo.shared.materials.Material
import indigo.shared.scenegraph.{ Graphic, Group, SceneUpdateFragment }
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Assets.Rooms
import lns.core.Assets.*
import lns.scenes.game.anything.{ AnythingViewModel, SolidModel }
import lns.scenes.game.room.RoomModel
import lns.scenes.game.room.door.{ DoorState, DoorView, Location }

object RoomView {

  def draw(context: FrameContext[StartupData], model: RoomModel, viewModel: RoomViewModel): Group =
    view(context, model, viewModel)

  def view(context: FrameContext[StartupData], model: RoomModel, viewModel: RoomViewModel): Group =
    Group()
      .addChild(RoomGraphic(context.startUpData))
      .addChild(doorView(context.startUpData, model, viewModel))
      .addChild(anythingView(context, model, viewModel))

  def doorView(startupData: StartupData, model: RoomModel, viewModel: RoomViewModel): Group =
    DoorView.view(startupData, model.doors, ())

  def anythingView(context: FrameContext[StartupData], model: RoomModel, viewModel: RoomViewModel): Group =
    model.anythings.foldLeft(Group())((s1, s2) =>
      s1.addChild(
        s2._2
          .view()
          .draw(
            context,
            s2._2,
            viewModel.anythings
              .get(s2._2.id)
              .getOrElse[AnythingViewModel[_] | Unit](())
          )
      )
    )
}

object RoomGraphic {

  def apply(startupData: StartupData): Graphic[Material.Bitmap] =
    Graphic(
      Rectangle(
        0,
        0,
        Rooms.roomSize,
        Rooms.roomSize
      ),
      1,
      Material.Bitmap(Rooms.EmptyRoom.name)
    )
}
