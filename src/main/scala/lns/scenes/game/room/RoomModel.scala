package lns.scenes.game.room

import indigo.*
import indigo.shared.{ FrameContext, Outcome }
import indigo.shared.datatypes.Rectangle
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Assets
import lns.core.Assets.Rooms
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ AnythingId, AnythingModel, DynamicState, SolidModel }
import lns.scenes.game.room.door.{ Door, DoorImplicit, DoorState, Location }
import lns.scenes.game.shot.ShotModel
import lns.scenes.game.room.door.DoorImplicit.*
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.enemy.EnemyModel
import lns.scenes.game.enemy.nerve.NerveModel
import lns.scenes.game.anything.given

import java.util.UUID
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
  val anythings: Map[AnythingId, AnythingModel]

  /**
   * Confine the character inside the limit of the room
   * @param anything
   *   the character bounding box
   * @return
   *   the bounded character's position
   */
  def boundPosition(position: BoundingBox): BoundingBox =
    Boundary.containerBound(floor, position)

  /**
   * Add a shot to the shot list
   * @param shot
   *   the shot to add
   * @return
   *   a new room with the new shot added
   */
  def addShot(shot: ShotModel): RoomModel =
    updateAnythings(anythings => anythings + (shot.id -> shot))

  def removeAnythings(anything: AnythingModel): RoomModel =
    updateAnythings(anythings => anythings.removed(anything.id))

  def updateEachAnything(f: AnythingModel => AnythingModel): RoomModel =
    updateAnythings(anythings => anythings.map(a => (a._1 -> f(a._2))))

  def updateAnythings(f: Map[AnythingId, AnythingModel] => Map[AnythingId, AnythingModel]): RoomModel =
    this match {
      case room: EmptyRoom =>
        room.copy(anythings = f(anythings))
      case room: ItemRoom =>
        room.copy(anythings = f(anythings))
      case room: ArenaRoom =>
        room.copy(anythings = f(anythings))
      case room: BossRoom =>
        room.copy(anythings = f(anythings))
      case _ => this
    }

  /**
   * Update the shot based on FrameContext
   * @param context
   *   the game context
   * @return
   *   a new room with the shot updated
   */
  def update(context: FrameContext[StartupData])(character: CharacterModel): Outcome[RoomModel] =

    val updatedAnythings: Outcome[Map[AnythingId, AnythingModel]] =
      anythings
        .map((id, any) => id -> any.update(context)(GameContext(this, character)))

    this match {
      case room: EmptyRoom =>
        updatedAnythings.map(anythings => room.copy(anythings = anythings))
      case room: ItemRoom =>
        updatedAnythings.map(anythings => room.copy(anythings = anythings))
      case room: ArenaRoom =>
        updatedAnythings.map(anythings => room.copy(anythings = anythings))
      case room: BossRoom =>
        updatedAnythings.map(anythings => room.copy(anythings = anythings))
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
    val anythings: Map[AnythingId, AnythingModel] = Map.empty
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
    val anythings: Map[AnythingId, AnythingModel] = Map.empty
) extends RoomModel {

  val doors =
    anythings.collect { case (_, e: EnemyModel) => e }.size match {
      case 0 => doorsLocations.open
      case _ => doorsLocations.close
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
    val anythings: Map[AnythingId, AnythingModel] = Map.empty
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
    val anythings: Map[AnythingId, AnythingModel] = Map.empty
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
      anythings: Map[AnythingId, AnythingModel]
  ): ArenaRoom = ArenaRoom(
    position,
    defaultFloor,
    locations,
    anythings
  )

  def itemRoom(position: Position, locations: DoorsLocations, anythings: Map[AnythingId, AnythingModel]): ItemRoom =
    ItemRoom(
      position,
      defaultFloor,
      locations,
      anythings
    )

  def bossRoom(position: Position, locations: DoorsLocations, anythings: Map[AnythingId, AnythingModel]): BossRoom =
    BossRoom(
      position,
      defaultFloor,
      locations,
      anythings
    )

  val defaultFloor: BoundingBox =
    BoundingBox(
      Vector2(0, 0),
      Vector2(
        Assets.Rooms.floorSize,
        Assets.Rooms.floorSize
      )
    )
}
