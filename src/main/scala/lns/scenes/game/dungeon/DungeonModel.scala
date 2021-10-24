package lns.scenes.game.dungeon

import lns.StartupData
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
  val row: Int
  val column: Int
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
  def nearPosition(grid: Grid)(position: Position)(location: Location): Option[Position] =
    location match {
      case Location.Left => Option(position._1, position._2 - 1)
      /*position % grid.column match {
          case 1 => None
          case _ => Option(position - 1)
        }*/
      case Location.Right => Option(position._1, position._2 + 1)
      /*position % grid.column match {
          case 0 => None
          case _ => Option(position + 1)
        }*/
      case Location.Above => Option(position._1 - 1, position._2)
      /*(position - grid.column) match {
          case x if x < 0 => None
          case x          => Option(x)
        }*/
      case Location.Below => Option(position._1 + 1, position._2)
      /*(position + grid.column) match {
          case x if x > (grid.row * grid.column) => None
          case x                                 => Option(x)
        }*/
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
    for (nearPosition <- nearPosition(grid)(position)(location) if grid.content.contains(nearPosition))
      yield grid.content(nearPosition)
}

enum RoomType:
  case Empty, Item, Arena, Boss

/**
 * Case class for the abstract dungeon developed by prolog. It stores a Map that contains all the room type and their
 * position
 */
case class BasicGrid(val row: Int, val column: Int, val content: Map[Position, RoomType]) extends Grid {
  override type Room = RoomType
}

/**
 * Case class of the entire Dungeon. It stores a Map that contains all the room and their position
 */
case class DungeonModel(val row: Int, val column: Int, val content: Map[Position, RoomModel]) extends Grid {
  override type Room = RoomModel
}

extension (dungeon: DungeonModel) {

  def room(position: Position)(location: Location): Option[RoomModel] = Grid.near(dungeon)(position)(location)

  /**
   * Retrieve the empty room of the dungeon where the game has to start
   * @return
   *   the first room in dungeon
   */
  def initialRoom: RoomModel = dungeon.content
    .collect { case (pos, EmptyRoom(_, _, _)) => pos }
    .map(pos => dungeon.content(pos))
    .head
}
