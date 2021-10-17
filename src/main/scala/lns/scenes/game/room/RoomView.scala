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
import lns.scenes.game.room.door.{ DoorPosition, DoorState, DoorView }

object RoomView {

  def draw(context: FrameContext[StartupData], model: RoomModel, viewModel: Unit): SceneUpdateFragment =
    SceneUpdateFragment(
      view(context.startUpData, model, viewModel)
    )

  def view(startupData: StartupData, model: RoomModel, viewModel: Unit): Group =
    Group()
      .addChild(RoomGraphic.roomGraphic(startupData))
      .addChild(doorView(startupData, model, viewModel))
      .withScale(Vector2(RoomGraphic.getScale(startupData.screenDimensions, Assets.Rooms.EmptyRoom.size)))
      .withRef(Assets.Rooms.EmptyRoom.size / 2, Assets.Rooms.EmptyRoom.size / 2)
      .moveTo(startupData.screenDimensions.center)

  def doorView(startupData: StartupData, model: RoomModel, viewModel: Unit): Group =
    DoorView.view(startupData, model.doors, ())

  def anythingView(startupData: StartupData, model: RoomModel, viewModel: Unit): Group =
    model match {
      case EnemyRoom(_, _, enemies, elements) => Group() //chiamare le rispettive view
      case ItemRoom(_, _, item)               => Group()
      case BossRoom(_, _, boss)               => Group()
    }
}

object RoomGraphic {

  def getScale(screenDimension: Rectangle, imageDimensions: Int): Double =
    Math.min(1.0 / imageDimensions * screenDimension.width, 1.0 / imageDimensions * screenDimension.height)

  def roomGraphic(startupData: StartupData): Graphic[Material.Bitmap] =
    Graphic(
      Rectangle(
        0,
        0,
        Rooms.EmptyRoom.size,
        Rooms.EmptyRoom.size
      ),
      1,
      Material.Bitmap(Rooms.EmptyRoom.name)
    )
}
