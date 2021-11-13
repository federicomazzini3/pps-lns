package lns.scenes.game.dungeon

import lns.StartupData
import lns.scenes.game.GameModel
import lns.scenes.game.room.door.Location
import lns.scenes.game.room.door.Location.*
import lns.scenes.game.room.door.DoorState.*
import lns.scenes.game.room.{ EmptyRoom, RoomModel }

/**
 * Trait for a grid that represent the dungeon map. The grid is made up of rows and columns and contains the positions
 * where the rooms are present
 */
trait Grid {
  type Room
  val content: Map[Position, Room]
}

type Position = (Int, Int)

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
  case Empty, Item, Arena, Boss

/**
 * Case class for the abstract dungeon developed by prolog. It stores a Map that contains all the room type and their
 * position
 */
case class BasicGrid(val content: Map[Position, RoomType]) extends Grid {
  override type Room = RoomType
}

/**
 * Case class of the entire Dungeon. It stores a Map that contains all the room and their position
 */
case class DungeonModel(val content: Map[Position, RoomModel]) extends Grid {
  override type Room = RoomModel

  def room(position: Position): Option[RoomModel] = Grid.in(this)(position)

  def nearRoom(position: Position)(location: Location): Option[RoomModel] = Grid.near(this)(position)(location)

  def nearPosition(position: Position)(location: Location): Option[(Int, Int)] =
    Grid.nearPosition(position)(location)

  /**
   * Retrieve the empty room of the dungeon where the game has to start
   * @return
   *   the first room in dungeon
   */
  def initialRoom: Position = content.collect { case (pos, EmptyRoom(_, _, _, _)) => pos }.head

  def updateRoom(position: Position)(updatedRoom: RoomModel): DungeonModel =
    this.copy(content = content.updated(position, updatedRoom))
}
