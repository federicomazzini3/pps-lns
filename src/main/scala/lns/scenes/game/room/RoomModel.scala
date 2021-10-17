package lns.scenes.game.room

import indigo.shared.datatypes.Rectangle
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Assets.Rooms
import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.room.door.{ Door, DoorPosition, DoorState, DoorImplicit }

type Door    = (DoorPosition, DoorState)
type Doors   = Map[DoorPosition, DoorState]
type Boss    = AnythingModel
type Enemy   = AnythingModel
type Item    = AnythingModel
type Element = AnythingModel

trait RoomModel {

  val floor: BoundingBox

  val doors: Doors

  def allowMoving(position: Vertex): Boolean = floor.contains(position)
}

trait ItemModel {
  val item: AnythingModel
}

trait EnemyModel {
  val enemies: Set[AnythingModel]
  val elements: Set[AnythingModel]
}

trait BossModel {
  val boss: AnythingModel
}

case class EmptyRoom(val floor: BoundingBox, val doors: Doors) extends RoomModel

case class EnemyRoom(
    val floor: BoundingBox,
    val doors: Doors,
    val enemies: Set[Enemy],
    val elements: Set[Element]
) extends RoomModel
    with EnemyModel

case class ItemRoom(val floor: BoundingBox, val doors: Doors, val item: Item) extends RoomModel with ItemModel
case class BossRoom(val floor: BoundingBox, val doors: Doors, val boss: Boss) extends RoomModel with BossModel

object RoomModel {
  import lns.scenes.game.room.door.DoorImplicit

  def initial(startupData: StartupData): EmptyRoom = EmptyRoom(
    internalBoundingBox(startupData.screenDimensions),
    Map((Door.left -> Door.close), (Door.right -> Door.close), Door.above -> Door.open, Door.below -> Door.lock)
  )

  def internalBoundingBox(screenDimension: Rectangle): BoundingBox = {
    val scale: Double             = RoomGraphic.getScale(screenDimension, Assets.Rooms.EmptyRoom.size)
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

}
