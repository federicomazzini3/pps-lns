package lns.scenes.game.room

import indigo.shared.datatypes.Rectangle
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Assets.Rooms
import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.room.door.{ Door, Location, DoorState, DoorImplicit }

type Door           = (Location, DoorState)
type DoorsLocations = Set[Location]
type Doors          = Map[Location, DoorState]
type Position       = (Int, Int)
type Boss           = AnythingModel
type Enemy          = AnythingModel
type Item           = AnythingModel
type Element        = AnythingModel

/**
 * Base model for a room
 */
trait RoomModel {

  /**
   * the position of the room inside the whole dungeon
   */
  val positionInDungeon: Position

  /**
   * the area where the elements are placed inside the room
   */
  val floor: BoundingBox

  /**
   * all the doors for a room
   */
  val doors: Doors

  /**
   * Confine the character inside the limit of the room
   * @param anything
   *   the character bounding box
   * @return
   *   the bounded character's position
   */
  def boundPosition(anything: BoundingBox): Vertex = Boundary.bound(floor, anything)
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
case class EmptyRoom(val positionInDungeon: Position, val floor: BoundingBox, val locations: DoorsLocations)
    extends RoomModel {
  val doors: Doors = locations.map(loc => loc -> DoorState.Open).toMap
}

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
    val positionInDungeon: Position,
    val floor: BoundingBox,
    val locations: DoorsLocations,
    val enemies: Set[Enemy],
    val elements: Set[Element]
) extends RoomModel
    with ArenaModel {

  /**
   * da modificare, quando implementerememo qualcosa con anything model non ci sarà più bisogno di controllare il null
   */
  val doors: Doors = enemies match {
    case null => locations.map(loc => loc -> DoorState.Open).toMap
    case _ =>
      enemies.size match {
        case 0 => locations.map(loc => loc -> DoorState.Open).toMap
        case _ => locations.map(loc => loc -> DoorState.Close).toMap
      }
  }
}

/**
 * The Room that contains one element to pick up
 * @param floor
 *   the dimension of floor
 * @param doors
 *   the set of the door
 * @param item
 *   the element to pick up
 */
case class ItemRoom(
    val positionInDungeon: Position,
    val floor: BoundingBox,
    val locations: DoorsLocations,
    val item: Item
) extends RoomModel
    with ItemModel {

  val doors: Doors = locations.map(loc => loc -> DoorState.Open).toMap
}

/**
 * The room where the character fights against the boss
 * @param floor
 *   the dimension of floor
 * @param doors
 *   the set of the door
 * @param boss
 *   the boss model
 */
case class BossRoom(
    val positionInDungeon: Position,
    val floor: BoundingBox,
    val locations: DoorsLocations,
    val boss: Boss
) extends RoomModel
    with BossModel {

  val doors: Doors = locations.map(loc => loc -> DoorState.Close).toMap
}

/**
 * Companion object, debug version for testing
 */
object RoomModel {
  import lns.scenes.game.room.door.*
  import lns.scenes.game.room.door.DoorImplicit.*
  import lns.scenes.game.room.door.DoorState.*
  import lns.scenes.game.room.door.Location.*

  def initial(startupData: StartupData): EmptyRoom = EmptyRoom(
    (0, 0),
    internalBoundingBox(startupData.screenDimensions),
    Left :+ Right :+ Above :+ Below
  )

  def emptyRoom(startupData: StartupData, position: Position, locations: DoorsLocations): EmptyRoom = EmptyRoom(
    position,
    internalBoundingBox(startupData.screenDimensions),
    locations
  )

  def arenaRoom(
      startupData: StartupData,
      position: Position,
      locations: DoorsLocations,
      enemies: Set[AnythingModel],
      elements: Set[AnythingModel]
  ): ArenaRoom = ArenaRoom(
    position,
    internalBoundingBox(startupData.screenDimensions),
    locations,
    enemies,
    elements
  )

  def itemRoom(startupData: StartupData, position: Position, locations: DoorsLocations, item: Item): ItemRoom =
    ItemRoom(
      position,
      internalBoundingBox(startupData.screenDimensions),
      locations,
      item
    )

  def bossRoom(startupData: StartupData, position: Position, locations: DoorsLocations, boss: Item): BossRoom =
    BossRoom(
      position,
      internalBoundingBox(startupData.screenDimensions),
      locations,
      boss
    )

  def internalBoundingBox(screenDimension: Rectangle): BoundingBox = {
    val scale: Double             = RoomGraphic.getScale(screenDimension, Assets.Rooms.EmptyRoom.size)
    val floorDimensionScaled: Int = (Rooms.EmptyRoom.floorSize * scale).toInt
    BoundingBox(
      Vertex(
        screenDimension.horizontalCenter - (floorDimensionScaled / 2),
        screenDimension.verticalCenter - (floorDimensionScaled / 2)
      ),
      Vertex(floorDimensionScaled, floorDimensionScaled)
    )
  }

}
