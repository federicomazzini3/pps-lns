package lns.scenes.game.room.door

import indigo.*
import indigo.shared.FrameContext
import indigo.shared.datatypes.{ Rectangle, Vector2 }
import indigo.shared.materials.Material
import indigo.shared.scenegraph.{ Graphic, Group, SceneUpdateFragment }
import indigoextras.geometry.*
import lns.StartupData
import lns.core.Assets
import lns.scenes.game.room.door.*
import lns.scenes.game.room.door.Location.*
import lns.scenes.game.room.door.DoorState.*

object DoorView {
  def view(startupData: StartupData, model: Doors, viewModel: Unit): Group =
    model.foldLeft(Group())((d1, d2) => d1.addChild(DoorGraphics(d2)))

  def draw(context: FrameContext[StartupData], model: Doors, viewModel: Unit): SceneUpdateFragment =
    SceneUpdateFragment(
      view(context.startUpData, model, viewModel)
    )
}

object DoorGraphics {

  import lns.core.Assets._

  val aboveBelowDoorCrop: Rectangle = Rectangle(0, 0, Doors.maxSize, Doors.minSize)

  val leftRigthDoorCrop: Rectangle = Rectangle(0, 0, Doors.minSize, Doors.maxSize)

  val left: Point   = Point(Rooms.wallSize - Doors.minSize - Rooms.offset, Rooms.roomSize / 2 - Doors.maxSize / 2)
  val right: Point  = Point(Rooms.roomSize - Rooms.wallSize + Rooms.offset, Rooms.roomSize / 2 - Doors.maxSize / 2)
  val top: Point    = Point(Rooms.roomSize / 2 - Doors.maxSize / 2, Rooms.wallSize - Doors.minSize - Rooms.offset)
  val bottom: Point = Point(Rooms.roomSize / 2 - Doors.maxSize / 2, Rooms.roomSize - Rooms.wallSize + Rooms.offset)

  def apply(door: Door): Group =
    Group().addChild(selectDoorGraphic(door))

  def selectDoorGraphic(door: Door): Graphic[Material.Bitmap] =
    door match {
      case (Left, Open) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorOpenWest)
          .moveTo(left)
      case (Left, Close) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorCloseWest)
          .moveTo(left)
      case (Left, Lock) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorLockWest)
          .moveTo(left)
      case (Right, Open) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorOpenEast)
          .moveTo(right)
      case (Right, Close) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorCloseEast)
          .moveTo(right)
      case (Right, Lock) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorLockEast)
          .moveTo(right)
      case (Above, Open) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorOpenNorth)
          .moveTo(top)
      case (Above, Close) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorCloseNorth)
          .moveTo(top)
      case (Above, Lock) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorLockNorth)
          .moveTo(top)
      case (Below, Open) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorOpenSouth)
          .moveTo(bottom)
      case (Below, Close) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorCloseSouth)
          .moveTo(bottom)
      case (Below, Lock) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorLockSouth)
          .moveTo(bottom)
    }

  def makeDoorGraphic(shape: Rectangle)(assetName: AssetName) =
    Graphic(
      shape,
      1,
      Material.Bitmap(assetName)
    )
}
