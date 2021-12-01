package lns.scenes.game.dungeon

import lns.StartupData
import lns.scenes.game.GameModel
import lns.scenes.game.room.door.Location
import lns.scenes.game.room.door.Location.*
import lns.scenes.game.room.door.DoorState.*
import lns.scenes.game.room.{ ArenaRoom, EmptyRoom, RoomModel }

type Position = (Int, Int)

/**
 * Trait for a grid that represent the dungeon map. The grid is made up of rows and columns and contains the positions
 * where the rooms are present
 */
trait Grid {
  type Room
  val content: Map[Position, Room]
  val initialRoom: Position
}

object Grid {

  /**
   * Retrieve a position in the grid next to another position
   * @param position
   *   the position of the room in the entire grid
   * @param location
   *   the location of the door behind the position is
   * @return
   *   a position if it's defined
   */
  def nearPosition(position: Position)(location: Location): Option[Position] =
    location match {
      case Location.Left  => Option(position._1 - 1, position._2)
      case Location.Right => Option(position._1 + 1, position._2)
      case Location.Above => Option(position._1, position._2 + 1)
      case Location.Below => Option(position._1, position._2 - 1)
    }

  /**
   * Retrieve a room in certain position
   * @param position
   *   the position of the room in the entire grid
   * @param location
   *   the position of the door behind the room is located
   * @return
   *   a room if it's defined
   */
  def near(grid: Grid)(position: Position)(location: Location): Option[grid.Room] =
    for (nearPosition <- nearPosition(position)(location) if grid.content.contains(nearPosition))
      yield grid.content(nearPosition)

  def in(grid: Grid)(position: Position): Option[grid.Room] = grid.content.get(position)
}

enum RoomType:
  case Start, Empty, Item, Arena, Boss

/**
 * Case class for the abstract dungeon developed by prolog. It stores a Map that contains all the room type and their
 * position
 */
case class BasicGrid(val content: Map[Position, RoomType]) extends Grid {
  override type Room = RoomType

  val initialRoom: Position = content.collect { case (pos, RoomType.Start) => pos }.head
}

/**
 * Case class of the entire Dungeon. It stores a Map that contains all the room and their position
 */
case class DungeonModel(val content: Map[Position, RoomModel], val initialRoom: Position) extends Grid {
  override type Room = RoomModel

  val generated = content.foldLeft(true)((generated, room) => room._2.generated && generated)

  val firstRoomToGenerate: Option[RoomModel] =
    content.collect {
      case (pos, room: RoomModel) if !room.generated => room
    }.headOption

  def room(position: Position): Option[RoomModel] = Grid.in(this)(position)

  def nearRoom(position: Position)(location: Location): Option[RoomModel] = Grid.near(this)(position)(location)

  def nearPosition(position: Position)(location: Location): Option[Position] =
    Grid.nearPosition(position)(location)

  def updateRoom(position: Position)(updatedRoom: RoomModel): DungeonModel =
    this.copy(content = content.updated(position, updatedRoom))
}
