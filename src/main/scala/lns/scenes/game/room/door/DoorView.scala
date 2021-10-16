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

type Door  = (DoorPosition, DoorState)
type Doors = Map[DoorPosition, DoorState]

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

  def apply(door: Door): Group =
    Group().addChild(selectDoorGraphic(door))

  def selectDoorGraphic(door: Door): Graphic[Material.Bitmap] =
    door match {
      case (Left, Open) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorOpenWest)
          .moveTo(50, 800)
      case (Left, Close) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorCloseWest)
          .moveTo(50, 800)
      case (Left, Lock) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorLockWest)
          .moveTo(50, 800)
      case (Right, Open) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorOpenEast)
          .moveTo(1700, 800)
      case (Right, Close) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorCloseEast)
          .moveTo(1700, 800)
      case (Right, Lock) =>
        makeDoorGraphic(leftRigthDoorCrop)(Doors.doorLockEast)
          .moveTo(1700, 800)
      case (Above, Open) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorOpenNorth)
          .moveTo(800, 50)
      case (Above, Close) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorCloseNorth)
          .moveTo(800, 50)
      case (Above, Lock) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorLockNorth)
          .moveTo(800, 50)
      case (Below, Open) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorOpenSouth)
          .moveTo(800, 1700)
      case (Below, Close) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorCloseSouth)
          .moveTo(800, 1700)
      case (Below, Lock) =>
        makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorLockSouth)
          .moveTo(800, 1700)
    }

  def makeDoorGraphic(shape: Rectangle)(assetName: AssetName) =
    Graphic(
      shape,
      1,
      Material.Bitmap(assetName)
    )
}
