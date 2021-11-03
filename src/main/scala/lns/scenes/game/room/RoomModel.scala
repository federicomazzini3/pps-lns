package lns.scenes.game.room

import indigo.shared.{ FrameContext, Outcome }
import indigo.shared.datatypes.Rectangle
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Assets.Rooms
import lns.scenes.game.anything.{ AnythingModel, given }
import lns.scenes.game.room.door.{ Door, DoorImplicit, DoorState, Location }
import lns.scenes.game.shot.ShotModel
import lns.scenes.game.room.door.DoorImplicit.*
import lns.scenes.game.character.CharacterModel

import scala.language.implicitConversions

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
   * the shots fired in a room
   */
  val anythings: Set[AnythingModel]

  /**
   * Confine the character inside the limit of the room
   * @param anything
   *   the character bounding box
   * @return
   *   the bounded character's position
   */
  def boundPosition(anything: BoundingBox): Vertex = Boundary.bound(floor, anything)

  /**
   * Add a shot to the shot list
   * @param shot
   *   the shot to add
   * @return
   *   a new room with the new shot added
   */
  def addShot(shot: ShotModel): RoomModel =
    val updatedAnythings = anythings + shot
    this match {
      case room: EmptyRoom =>
        room.copy(anythings = updatedAnythings)
      case room: ItemRoom =>
        room.copy(anythings = updatedAnythings)
      case room: ArenaRoom =>
        room.copy(anythings = updatedAnythings)
      case room: BossRoom =>
        room.copy(anythings = updatedAnythings)
      case _ => this
    }

  /**
   * Call the method update in all of anythings in a room. Can be override from subclasses for more specific behavior
   * @param context
   * @return
   *   a new updated set of anything model
   */
  def updateAnythings(context: FrameContext[StartupData])(character: CharacterModel): Outcome[Set[AnythingModel]] =
    anythings
      .map(any => any.update(context)(this)(character))

  /**
   * Update the shot based on FrameContext
   * @param context
   *   the game context
   * @return
   *   a new room with the shot updated
   */
  def update(context: FrameContext[StartupData])(character: CharacterModel): Outcome[RoomModel] =
    val out = updateAnythings(context)(character)

    this match {
      case room: EmptyRoom =>
        out.map(anythings => room.copy(anythings = anythings))
      case room: ItemRoom =>
        out.map(anythings => room.copy(anythings = anythings))
      case room: ArenaRoom =>
        out.map(anythings => room.copy(anythings = anythings))
      case room: BossRoom =>
        out.map(anythings => room.copy(anythings = anythings))
      case _ => Outcome(this)
    }
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
    val doorsLocations: DoorsLocations,
    val anythings: Set[AnythingModel] = Set.empty
) extends RoomModel {
  val doors = doorsLocations.open
}

/**
 * The room where the character fight against monsters
 * @param floor
 *   the dimension of floor
 * @param doors
 *   the set of the door
 * @param anythings
 *   the set of anythings inside a room
 */
case class ArenaRoom(
    val positionInDungeon: Position,
    val floor: BoundingBox,
    val doorsLocations: DoorsLocations,
    val anythings: Set[AnythingModel] = Set.empty
) extends RoomModel {

  //da filtrare con i nemici (quando ci saranno)
  val doors = anythings.size match {
    case 0 => doorsLocations.open
    case _ => doorsLocations.open //close
  }
}

/**
 * The Room that contains one element to pick up
 * @param floor
 *   the dimension of floor
 * @param doors
 *   the set of the door
 * @param anythings
 *   the set of anythings inside a room
 */
case class ItemRoom(
    val positionInDungeon: Position,
    val floor: BoundingBox,
    val doorsLocations: DoorsLocations,
    val anythings: Set[AnythingModel] = Set.empty
) extends RoomModel {

  val doors = doorsLocations.open
}

/**
 * The room where the character fights against the boss
 * @param floor
 *   the dimension of floor
 * @param doors
 *   the set of the door
 * @param anythings
 *   the set of anythings inside a room
 */
case class BossRoom(
    val positionInDungeon: Position,
    val floor: BoundingBox,
    val doorsLocations: DoorsLocations,
    val anythings: Set[AnythingModel] = Set.empty
) extends RoomModel {

  val doors = doorsLocations.close
}

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
    Left :+ Right :+ Above :+ Below
  )

  def emptyRoom(position: Position, locations: DoorsLocations): EmptyRoom = EmptyRoom(
    position,
    defaultFloor,
    locations
  )

  def arenaRoom(
      position: Position,
      locations: DoorsLocations,
      anythings: Set[AnythingModel]
  ): ArenaRoom = ArenaRoom(
    position,
    defaultFloor,
    locations,
    anythings
  )

  def itemRoom(position: Position, locations: DoorsLocations, anythings: Set[AnythingModel]): ItemRoom =
    ItemRoom(
      position,
      defaultFloor,
      locations,
      anythings
    )

  def bossRoom(position: Position, locations: DoorsLocations, anythings: Set[AnythingModel]): BossRoom =
    BossRoom(
      position,
      defaultFloor,
      locations,
      anythings
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
