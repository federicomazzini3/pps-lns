package lns.scenes.game.dungeon

import lns.StartupData
import lns.scenes.game.room.door.DoorPosition
import lns.scenes.game.room.door.DoorPosition.*
import lns.scenes.game.room.door.DoorState.*
import lns.scenes.game.room.{ EmptyRoom, RoomModel }

/**
 * Case class of the entire Dungeon. It stores a Map that contains all the room and their position
 */
type Position = Int
case class Dungeon(val row: Int, val column: Int, val grid: Map[Position, RoomModel]) {

  /**
   * Aetrieve the empty room of the dungeon where the game has to start
   * @return
   *   the first room in dungeon
   */
  def initialRoom: Position = grid.filter(_._2.getClass == EmptyRoom).map(_._1).head

  /**
   * Retrieve a room in certain position
   * @param roomPosition
   *   the position of the room in the entire grid
   * @param doorPosition
   *   the position of the door behind the room is located
   * @return
   *   a room if it's defined
   */
  def room(roomPosition: Position)(doorPosition: DoorPosition): Option[RoomModel] =
    position(roomPosition)(doorPosition)
      .map(p => grid(p))

  /**
   * Retrieve a position in the grid next to another position
   * @param roomPosition
   *   the position of the room in the entire grid
   * @param doorPosition
   *   the position of the door behind the position is
   * @return
   *   a position if it's defined
   */
  def position(roomPosition: Position)(doorPosition: DoorPosition): Option[Position] = doorPosition match {
    case Left =>
      roomPosition % column match {
        case 1 => None
        case _ => Option(roomPosition + 1)
      }
    case Right =>
      roomPosition % column match {
        case 0 => None
        case _ => Option(roomPosition - 1)
      }
    case Above =>
      (roomPosition - column) match {
        case x if x < 0 => None
        case x          => Option(x)
      }
    case Below =>
      (roomPosition + column) match {
        case x if x > 0 => None
        case x          => Option(x)
      }
  }
}
