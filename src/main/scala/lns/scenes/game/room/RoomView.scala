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
import lns.scenes.game.room.RoomModel
import lns.scenes.game.room.door.{ DoorState, DoorView, Location }
import lns.scenes.game.shot.ShotView

object RoomView {

  def draw(context: FrameContext[StartupData], model: RoomModel, viewModel: Unit): Group =
    view(context, model, viewModel)

  def view(context: FrameContext[StartupData], model: RoomModel, viewModel: Unit): Group =
    Group()
      .addChild(RoomGraphic.roomGraphic(context.startUpData))
      .addChild(doorView(context.startUpData, model, viewModel))
      .addChild(shotView(context, model, viewModel))

  def doorView(startupData: StartupData, model: RoomModel, viewModel: Unit): Group =
    DoorView.view(startupData, model.doors, ())

  def shotView(context: FrameContext[StartupData], model: RoomModel, viewModel: Unit): Group =
    model.shots.foldLeft(Group())((s1, s2) => s1.addChild(ShotView().draw(context, s2, ())))

  def anythingView(startupData: StartupData, model: RoomModel, viewModel: Unit): Group =
    model match {
      case ArenaRoom(_, _, _, enemies, elements, _) => Group() //chiamare le rispettive view
      case ItemRoom(_, _, _, item, _)               => Group()
      case BossRoom(_, _, _, boss, _)               => Group()
    }
}

object RoomGraphic {

  def roomGraphic(startupData: StartupData): Graphic[Material.Bitmap] =
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
