package lns.scenes.game.room

import indigo.*
import indigo.shared.{ FrameContext, Outcome }
import indigo.shared.datatypes.Rectangle
import indigoextras.geometry.BoundingBox
import lns.StartupData
import lns.core.Assets
import lns.core.Assets.Rooms
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ AnythingId, AnythingModel, DynamicState, SolidModel, given }
import lns.scenes.game.room.door.{ Door, DoorImplicit, DoorState, Location }
import lns.scenes.game.shot.ShotModel
import lns.scenes.game.room.door.DoorImplicit.*
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.enemy.EnemyModel
import lns.scenes.game.enemy.nerve.NerveModel

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
  def boundPosition(model: AnythingModel, position: BoundingBox)(character: CharacterModel): BoundingBox =
    def differentBB(bb1: BoundingBox, bb2: BoundingBox) =
      bb1.left != bb2.left && bb1.right != bb2.right &&
        bb1.top != bb2.top && bb1.bottom != bb2.bottom

    val positionBounded = Boundary.containerBound(floor, position)
    positionBounded

  /*val posBounded = Boundary.containerBound(floor, position)
    model match {
      case s: SolidModel =>
        val shotArea = s.generateNewShotArea(posBounded)
        anythings.values
          .collect {
            case a: SolidModel if !a.crossable && (s match {
                  case a: ShotModel => differentBB(a.shotArea, posBounded)
                  case _            => differentBB(a.boundingBox, posBounded)
                }) =>
              a
          }
          .foldLeft(posBounded)((anything, element) =>
            (model, element) match {
              case (elem1: SolidModel, elem2: ShotModel) =>
                Boundary.elementBound(element.shotArea, elem2.boundingBox)
              case (elem1: ShotModel, elem2: SolidModel) =>
                Boundary.elementBound(element.boundingBox, anything)
            }
          )
      case _ => posBounded
    }

    anythings.values
      .collect {

        case a: SolidModel if !a.crossable && (model match {
              case a: ShotModel => differentBB(a.shotArea, posBounded)
              case _            => differentBB(a.boundingBox, posBounded)
            }) =>
          a
      }

      model match {
        case a: ShotModel => .foldLeft(posBounded)((anything, element) =>
        case : => .foldLeft(shotArea)((anything, element) =>
      }
      .foldLeft(posBounded)((anything, element) =>
        model match {
          case a: ShotModel =>
            Boundary.elementBound(element.shotArea, anything)
          case _ =>
            Boundary.elementBound(element.boundingBox, anything)
        }
      )*/

  /**
   * Add a shot to the shot list
   * @param shot
   *   the shot to add
   * @return
   *   a new room with the new shot added
   */
  def addShot(shot: ShotModel): RoomModel =
    val updatedAnythings = anythings + (shot.id -> shot)
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
  def updateAnythings(
      context: FrameContext[StartupData]
  )(character: CharacterModel): Outcome[Map[AnythingId, AnythingModel]] =
    val gameContext = GameContext(this, character)
    anythings
      .map((id, any) => id -> any.update(context)(gameContext))

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
