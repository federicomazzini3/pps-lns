package lns.scenes.game.room

import indigo.shared.{ FrameContext, Outcome }
import indigo.shared.datatypes.Rectangle
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Assets.Rooms
import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.room.door.{ Door, DoorImplicit, DoorState, Location }
import lns.scenes.game.shot.ShotModel

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

  /**
   * the shot explode in a room
   */
  val shots: List[ShotModel]

  /**
   * Add a shot to the shot list
   * @param shot
   *   the shot to add
   * @return
   *   a new room with the new shot added
   */
  def addShot(shot: ShotModel): RoomModel =
    RoomCopy(this)(shots :+ shot)

  /**
   * Update the shot based on FrameContext
   * @param context
   *   the game context
   * @return
   *   a new room with the shot updated
   */
  def update(context: FrameContext[StartupData]): Outcome[RoomModel] =
    Outcome(
      RoomCopy(this)(ShotModel.updateShots(shots)(context)(this))
    )
}

/**
 * Extension for room with one item
 */
trait ItemModel {
  room =>
  RoomModel
  val item: AnythingModel
}

/**
 * Extension for room with enemies and other elements
 */
trait ArenaModel {
  room =>
  RoomModel
  val enemies: Set[AnythingModel]
  val elements: Set[AnythingModel]
}

/**
 * Extension for room with boss
 */
trait BossModel {
  room =>
  RoomModel
  val boss: AnythingModel
}

/**
 * Base room
 * @param floor
 *   the dimension of floor
 * @param doors
 *   the set of the door
 */
case class EmptyRoom(
    val positionInDungeon: Position,
    val floor: BoundingBox,
    val doors: Doors,
    val shots: List[ShotModel] = List.empty
) extends RoomModel

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
    val doors: Doors,
    val enemies: Set[Enemy],
    val elements: Set[Element],
    val shots: List[ShotModel] = List.empty
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
case class ItemRoom(
    val positionInDungeon: Position,
    val floor: BoundingBox,
    val doors: Doors,
    val item: Item,
    val shots: List[ShotModel] = List.empty
) extends RoomModel
    with ItemModel

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
    val doors: Doors,
    val boss: Boss,
    val shots: List[ShotModel] = List.empty
) extends RoomModel
    with BossModel

/**
 * Companion object, debug version for testing
 */
object RoomModel {
  import lns.scenes.game.room.door.*
  import lns.scenes.game.room.door.DoorImplicit.*
  import lns.scenes.game.room.door.DoorState.*
  import lns.scenes.game.room.door.Location.*

  def initial(): EmptyRoom = EmptyRoom(
    (0, 0),
    defaultFloor,
    (Left -> Open) :+ (Right -> Close) :+ (Above -> Lock) :+ (Below -> Open)
  )

  def emptyRoom(position: Position, locations: DoorsLocations): EmptyRoom = EmptyRoom(
    position,
    defaultFloor,
    InitialDoorSetup.empty(locations)
  )

  def arenaRoom(
      position: Position,
      locations: DoorsLocations,
      enemies: Set[AnythingModel],
      elements: Set[AnythingModel]
  ): ArenaRoom = ArenaRoom(
    position,
    defaultFloor,
    InitialDoorSetup.arena(locations)(enemies),
    enemies,
    elements
  )

  def itemRoom(position: Position, locations: DoorsLocations, item: Item): ItemRoom =
    ItemRoom(
      position,
      defaultFloor,
      InitialDoorSetup.item(locations),
      item
    )

  def bossRoom(position: Position, locations: DoorsLocations, boss: Item): BossRoom =
    BossRoom(
      position,
      defaultFloor,
      InitialDoorSetup.boss(locations),
      boss
    )

  val defaultFloor: BoundingBox =
    BoundingBox(
      Vertex(0, 0),
      Vertex(
        Assets.Rooms.floorSize,
        Assets.Rooms.floorSize
      )
    )
}

object RoomCopy {
  def apply(roomModel: RoomModel)(newShots: List[ShotModel]): RoomModel =
    roomModel match {
      case room: ArenaRoom =>
        room.copy(shots = newShots)
      case room: BossRoom =>
        room.copy(shots = newShots)
      case room: EmptyRoom =>
        room.copy(shots = newShots)
      case room: ItemRoom =>
        room.copy(shots = newShots)
    }
}
