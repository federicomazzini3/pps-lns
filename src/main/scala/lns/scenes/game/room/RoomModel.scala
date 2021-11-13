package lns.scenes.game.room

import indigo.*
import indigo.shared.{ FrameContext, Outcome }
import indigo.shared.datatypes.Rectangle
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Assets
import lns.core.Assets.Rooms
import lns.scenes.game.GameContext
import lns.scenes.game.dungeon.*
import lns.scenes.game.anything.{ AnythingId, AnythingModel, DynamicState, SolidModel }
import lns.scenes.game.room.door.*
import lns.scenes.game.shot.ShotModel
import lns.scenes.game.room.door.DoorImplicit.*
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.enemy.EnemyModel
import lns.scenes.game.enemy.nerve.NerveModel
import lns.scenes.game.anything.given
import java.util.UUID
import scala.language.implicitConversions

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

  /**
   * Remove an anyhthing from the room
   * @param anything
   * @return
   *   a new room without the specific anything
   */
  def removeAnythings(anything: AnythingModel): RoomModel =
    updateAnythings(anythings => anythings.removed(anything.id))

  /**
   * Update the anythings of a room with a function
   * @param updateFunc
   *   the update function to update one anything with
   * @return
   *   a new room with anythings updated
   */
  def updateEachAnything(updateFunc: AnythingModel => AnythingModel): RoomModel =
    updateAnythings(anythings => anythings.map(a => (a._1 -> updateFunc(a._2))))

  /**
   * update the anythings of a room in their entirety
   * @param updateFunc
   *   the update function to update the anythings with
   * @return
   *   a new room with the anything updated
   */
  def updateAnythings(updateFunc: Map[AnythingId, AnythingModel] => Map[AnythingId, AnythingModel]): RoomModel =
    this match {
      case room: EmptyRoom =>
        room.copy(anythings = updateFunc(anythings))
      case room: ItemRoom =>
        room.copy(anythings = updateFunc(anythings))
      case room: ArenaRoom =>
        room.copy(anythings = updateFunc(anythings))
      case room: BossRoom =>
        room.copy(anythings = updateFunc(anythings))
      case _ => this
    }

  /**
   * Update the anyhthings state inside a room
   * @param context
   *   the FrameContext
   * @param character
   *   the CharacterModel
   * @return
   *   a new room with the anything state updated
   */
  def update(context: FrameContext[StartupData])(character: CharacterModel): Outcome[RoomModel] =
    val updatedAnythings: Outcome[Map[AnythingId, AnythingModel]] =
      anythings
        .map((id, any) => id -> any.update(context)(GameContext(this, character)))

    for (updated <- updatedAnythings)
      yield this.updateAnythings(anythings => updated)
}

/**
 * The base initial room
 * @param positionInDungeon
 *   the position of the room inside the entire dungeon
 * @param floor
 *   the floor Bounding Box
 * @param doorsLocations
 *   the location in which the room has the door
 * @param anythings
 *   a collection of anything inside the room
 */
case class EmptyRoom(
    positionInDungeon: Position,
    floor: BoundingBox,
    doorsLocations: DoorsLocations,
    anythings: Map[AnythingId, AnythingModel] = Map.empty
) extends RoomModel {
  val doors = doorsLocations.open
}

/**
 * The room where the character fight against monsters
 * @param positionInDungeon
 *   the position of the room inside the entire dungeon
 * @param floor
 *   the floor Bounding Box
 * @param doorsLocations
 *   the location in which the room has the door
 * @param anythings
 *   a collection of anything inside the room
 */
case class ArenaRoom(
    positionInDungeon: Position,
    floor: BoundingBox,
    doorsLocations: DoorsLocations,
    anythings: Map[AnythingId, AnythingModel] = Map.empty
) extends RoomModel {

  val doors =
    anythings.collect { case (_, e: EnemyModel) => e }.size match {
      case 0 => doorsLocations.open
      case _ => doorsLocations.close
    }
}

/**
 * The Room that contains one element to pick up
 * @param positionInDungeon
 *   the position of the room inside the entire dungeon
 * @param floor
 *   the floor Bounding Box
 * @param doorsLocations
 *   the location in which the room has the door
 * @param anythings
 *   a collection of anything inside the room
 */
case class ItemRoom(
    positionInDungeon: Position,
    floor: BoundingBox,
    doorsLocations: DoorsLocations,
    anythings: Map[AnythingId, AnythingModel] = Map.empty
) extends RoomModel {

  val doors = doorsLocations.open
}

/**
 * The room where the character fights against the boss
 * @param positionInDungeon
 *   the position of the room inside the entire dungeon
 * @param floor
 *   the floor Bounding Box
 * @param doorsLocations
 *   the location in which the room has the door
 * @param anythings
 *   a collection of anything inside the room
 */
case class BossRoom(
    positionInDungeon: Position,
    floor: BoundingBox,
    doorsLocations: DoorsLocations,
    anythings: Map[AnythingId, AnythingModel] = Map.empty
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

  def arenaRoom(position: Position, locations: DoorsLocations, anythings: Map[AnythingId, AnythingModel]): ArenaRoom =
    ArenaRoom(
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
