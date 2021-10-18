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

/**
 * Base model for a room
 */
trait RoomModel {

  /**
   * the area where the elements are placed inside the room
   */
  val floor: BoundingBox

  /**
   * all the doors for a room
   */
  val doors: Doors
}

/**
 * Extension for room with one item
 */
trait ItemModel {
  val item: AnythingModel
}

/**
 * Extension for room with enemies and other elements
 */
trait ArenaModel {
  val enemies: Set[AnythingModel]
  val elements: Set[AnythingModel]
}

/**
 * Extension for room with boss
 */
trait BossModel {
  val boss: AnythingModel
}

/**
 * Base room
 * @param floor
 *   the dimension of floor
 * @param doors
 *   the set of the door
 */
case class EmptyRoom(val floor: BoundingBox, val doors: Doors) extends RoomModel

/**
 * The room where the character fight against monsters
 * @param floor
 *   the dimension of floor
 * @param doors
 *   the set of the door
 * @param enemies
 *   the set of enemies
 * @param elements
 *   the set of elements
 */
case class ArenaRoom(
    val floor: BoundingBox,
    val doors: Doors,
    val enemies: Set[Enemy],
    val elements: Set[Element]
) extends RoomModel
    with ArenaModel

/**
 * The Room that contains one element to pick up
 * @param floor
 *   the dimension of floor
 * @param doors
 *   the set of the door
 * @param item
 *   the element to pick up
 */
case class ItemRoom(val floor: BoundingBox, val doors: Doors, val item: Item) extends RoomModel with ItemModel

/**
 * The room where the character fights against the boss
 * @param floor
 *   the dimension of floor
 * @param doors
 *   the set of the door
 * @param boss
 *   the boss model
 */
case class BossRoom(val floor: BoundingBox, val doors: Doors, val boss: Boss) extends RoomModel with BossModel

/**
 * Companion object, debug version for testing
 */
object RoomModel {
  import lns.scenes.game.room.door.DoorImplicit.*

  def initial(startupData: StartupData): EmptyRoom = EmptyRoom(
    internalBoundingBox(startupData.screenDimensions),
    (Door.left -> Door.close) :+ (Door.right -> Door.close) :+ (Door.above -> Door.open) :+ (Door.below -> Door.lock)
  )

  def internalBoundingBox(screenDimension: Rectangle): BoundingBox = {
    val scale: Double             = RoomGraphic.getScale(screenDimension, Assets.Rooms.EmptyRoom.size)
    val floorDimensionScaled: Int = (Rooms.EmptyRoom.floorSize * scale).toInt
    val floorWidthScaled: Int     = ((Rooms.EmptyRoom.floorSize - 135) * scale).toInt
    val floorHeightScaled: Int    = ((Rooms.EmptyRoom.floorSize - 170) * scale).toInt
    BoundingBox(
      Vertex(
        screenDimension.horizontalCenter - (floorWidthScaled / 2),
        screenDimension.verticalCenter - (floorHeightScaled / 2)
      ),
      Vertex(floorWidthScaled, floorHeightScaled)
    )
  }

}
