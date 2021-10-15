package lns.scenes.game.room

import indigo.*
import indigoextras.geometry.*
import lns.StartupData
import lns.core.Assets
import lns.core.Assets.{ Doors, Rooms }
import lns.scenes.game.anything.AnythingModel

trait Room {

  val boundingBox: BoundingBox

  def allowMoving: Vertex => Boolean = (anything: Vertex) => (boundingBox.contains(anything))

  def update(room: Room): Room = room
}

trait Doors {
  room: Room =>
  val leftDoor: Option[Room]
  val rightDoor: Option[Room]
  val aboveDoor: Option[Room]
  val belowDoor: Option[Room]
  def doorState: DoorState
  def checkAvailableNeighbors(location: DoorLocation): Boolean = location match {
    case DoorLocation.Left  => room.leftDoor.isDefined
    case DoorLocation.Right => room.rightDoor.isDefined
    case DoorLocation.Above => room.aboveDoor.isDefined
    case DoorLocation.Below => room.belowDoor.isDefined
  }
}

trait Object {
  doors: Room with Doors =>
  val anything: AnythingModel

  override val doorState: DoorState = DoorState.Closed //to modify with a function: if anything is picked or died
}

trait Enemies {
  doors: Room with Doors =>
  val anythings: Set[AnythingModel]

  override val doorState: DoorState = DoorState.Closed //to modify with a function: if all anything are picked or died
}

trait Boss {
  doors: Room with Doors =>
  val anything: AnythingModel

  override val doorState: DoorState = DoorState.Closed //to modify with a function: if anything is picked or died
}

trait RoomViewModel

object RoomView {

  def view(startupData: StartupData, model: EmptyRoom, viewModel: Unit): Group =
    Group()
      .addChild(RoomGraphics.roomGraphic(startupData))
      .addChild(RoomGraphics.leftDoorGraphic(model.leftDoor)(model.doorState))
      .addChild(RoomGraphics.rightDoorGraphic(model.rightDoor)(model.doorState))
      .addChild(RoomGraphics.aboveDoorGraphic(model.aboveDoor)(model.doorState))
      .addChild(RoomGraphics.belowDoorGraphic(model.belowDoor)(model.doorState))
      .withScale(Vector2(RoomGraphics.getScale(startupData.screenDimensions, Assets.Rooms.EmptyRoom.size)))
      .withRef(Assets.Rooms.EmptyRoom.size / 2, Assets.Rooms.EmptyRoom.size / 2)
      .moveTo(startupData.screenDimensions.center)

  def draw(context: FrameContext[StartupData], model: EmptyRoom, viewModel: Unit): SceneUpdateFragment =
    SceneUpdateFragment(
      view(context.startUpData, model, viewModel)
    )
}

enum DoorLocation:
  case Left, Right, Above, Below

enum DoorState:
  case Open, Closed, Locked

case class EmptyRoom(
    val boundingBox: BoundingBox,
    val leftDoor: Option[EmptyRoom] = None,
    val rightDoor: Option[EmptyRoom] = None,
    val aboveDoor: Option[EmptyRoom] = None,
    val belowDoor: Option[EmptyRoom] = None
) extends Room
    with Doors {
  override def doorState: DoorState = DoorState.Open
}

case class ObjectRoom(
    val boundingBox: BoundingBox,
    val leftDoor: Option[EmptyRoom] = None,
    val rightDoor: Option[EmptyRoom] = None,
    val aboveDoor: Option[EmptyRoom] = None,
    val belowDoor: Option[EmptyRoom] = None,
    val anything: AnythingModel
) extends Room
    with Doors
    with Object

case class EnemyRoom(
    val boundingBox: BoundingBox,
    val leftDoor: Option[EmptyRoom] = None,
    val rightDoor: Option[EmptyRoom] = None,
    val aboveDoor: Option[EmptyRoom] = None,
    val belowDoor: Option[EmptyRoom] = None,
    val anythings: Set[AnythingModel]
) extends Room
    with Doors
    with Enemies

case class BossRoom(
    val boundingBox: BoundingBox,
    val leftDoor: Option[EmptyRoom] = None,
    val rightDoor: Option[EmptyRoom] = None,
    val aboveDoor: Option[EmptyRoom] = None,
    val belowDoor: Option[EmptyRoom] = None,
    val anything: AnythingModel
) extends Room
    with Doors
    with Boss

extension (room: EmptyRoom) {

  def addLeftRoom(toAddRoom: EmptyRoom): EmptyRoom =
    if (room.checkAvailableNeighbors(DoorLocation.Left)) room
    else room.copy(leftDoor = Option(toAddRoom.copy(rightDoor = Option(room))))

  def addRightRoom(toAddRoom: EmptyRoom): EmptyRoom =
    if (room.checkAvailableNeighbors(DoorLocation.Right)) room
    else room.copy(rightDoor = Option(toAddRoom.copy(leftDoor = Option(room))))

  def addAboveRoom(toAddRoom: EmptyRoom): EmptyRoom =
    if (room.checkAvailableNeighbors(DoorLocation.Above)) room
    else room.copy(aboveDoor = Option(toAddRoom.copy(belowDoor = Option(room))))

  def addBelowRoom(toAddRoom: EmptyRoom): EmptyRoom =
    if (room.checkAvailableNeighbors(DoorLocation.Below)) room
    else room.copy(belowDoor = Option(toAddRoom.copy(aboveDoor = Option(room))))
}

object Room {
  import RoomGraphics.*
  def initial(startupData: StartupData): EmptyRoom =
    EmptyRoom(
      internalBoundingBox(startupData.screenDimensions)
    ).addLeftRoom(
      EmptyRoom(
        internalBoundingBox(startupData.screenDimensions)
      )
    ).addRightRoom(
      EmptyRoom(
        internalBoundingBox(startupData.screenDimensions)
      )
    )
}

object RoomGraphics {

  import Assets._

  def internalBoundingBox(screenDimension: Rectangle): BoundingBox = {
    val scale: Double             = getScale(screenDimension, Assets.Rooms.EmptyRoom.size)
    val floorDimensionScaled: Int = (Rooms.EmptyRoom.floorSize * scale).toInt
    val floorWidthScaled: Int     = ((Rooms.EmptyRoom.floorSize - 135) * scale).toInt
    val floorHeightScaled: Int    = ((Rooms.EmptyRoom.floorSize - 170) * scale).toInt
    BoundingBox(
      Vertex(
        screenDimension.horizontalCenter - (floorDimensionScaled / 2),
        screenDimension.verticalCenter - (floorDimensionScaled / 2)
      ),
      Vertex(floorWidthScaled, floorHeightScaled)
    )
  }

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

  val aboveBelowDoorCrop: Rectangle = Rectangle(0, 0, Doors.maxSize, Doors.minSize)

  val leftRigthDoorCrop: Rectangle = Rectangle(0, 0, Doors.minSize, Doors.maxSize)

  def makeDoorGraphic(shape: Rectangle)(assetName: AssetName): Graphic[Material.Bitmap] =
    Graphic(
      shape,
      1,
      Material.Bitmap(assetName)
    )

  def leftDoorGraphic(optRoom: Option[EmptyRoom])(doorState: DoorState): Group =
    optRoom match {
      case Some(_) =>
        doorState match {
          case DoorState.Open =>
            Group().addChild(
              makeDoorGraphic(leftRigthDoorCrop)(Doors.doorOpenWest)
                .moveTo(50, 800)
            )
          case DoorState.Closed =>
            Group().addChild(
              makeDoorGraphic(leftRigthDoorCrop)(Doors.doorCloseWest)
                .moveTo(50, 800)
            )
          case DoorState.Locked =>
            Group().addChild(
              makeDoorGraphic(leftRigthDoorCrop)(Doors.doorLockWest)
                .moveTo(50, 800)
            )
        }
      case None => Group()
    }

  def rightDoorGraphic(optRoom: Option[EmptyRoom])(doorState: DoorState): Group =
    optRoom match {
      case Some(_) =>
        doorState match {
          case DoorState.Closed =>
            Group().addChild(
              makeDoorGraphic(leftRigthDoorCrop)(Doors.doorCloseEast)
                .moveTo(1700, 800)
            )
          case DoorState.Open =>
            Group().addChild(
              makeDoorGraphic(leftRigthDoorCrop)(Doors.doorOpenEast)
                .moveTo(1700, 800)
            )
          case DoorState.Locked =>
            Group().addChild(
              makeDoorGraphic(leftRigthDoorCrop)(Doors.doorLockEast)
                .moveTo(1700, 800)
            )
        }
      case None => Group()
    }

  def aboveDoorGraphic(optRoom: Option[EmptyRoom])(doorState: DoorState): Group =
    optRoom match {
      case Some(_) =>
        doorState match {
          case DoorState.Closed =>
            Group().addChild(
              makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorCloseNorth)
                .moveTo(800, 50)
            )
          case DoorState.Open =>
            Group().addChild(
              makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorOpenNorth)
                .moveTo(800, 50)
            )
          case DoorState.Locked =>
            Group().addChild(
              makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorLockSouth)
                .moveTo(800, 50)
            )
        }
      case None => Group()
    }

  def belowDoorGraphic(optRoom: Option[EmptyRoom])(doorState: DoorState): Group =
    optRoom match {
      case Some(_) =>
        doorState match {
          case DoorState.Closed =>
            Group().addChild(
              makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorCloseSouth)
                .moveTo(800, 1700)
            )
          case DoorState.Open =>
            Group().addChild(
              makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorOpenSouth)
                .moveTo(800, 1700)
            )
          case DoorState.Locked =>
            Group().addChild(
              makeDoorGraphic(aboveBelowDoorCrop)(Doors.doorLockSouth)
                .moveTo(800, 1700)
            )
        }
      case None => Group()
    }
}
