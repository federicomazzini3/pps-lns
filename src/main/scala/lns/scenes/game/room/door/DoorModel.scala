package lns.scenes.game.room.door

import lns.scenes.game.room.door.Door.updateWith
import lns.scenes.game.room.door.{ DoorPosition, DoorState }

/**
 * A model for the position of the door in a room
 */
sealed trait DoorPosition
case object Left  extends DoorPosition
case object Right extends DoorPosition
case object Above extends DoorPosition
case object Below extends DoorPosition

/**
 * A model for the state of the door in a room
 */
sealed trait DoorState
case object Open  extends DoorState
case object Close extends DoorState
case object Lock  extends DoorState

/**
 * Object that describe a door
 */
object Door {

  val left: DoorPosition  = Left
  val right: DoorPosition = Right
  val above: DoorPosition = Above
  val below: DoorPosition = Below

  val open: DoorState  = Open
  val close: DoorState = Close
  val lock: DoorState  = Lock

  /**
   * @param door
   *   a tuple of a position and a door
   * @return
   *   a new Map with the door
   */
  def apply(door: (DoorPosition, DoorState)) = Map(door)

  /**
   * @param doors
   *   the map of doors to update
   * @param toAddDoor
   *   the door to add
   * @return
   *   a new map with the door added
   */
  def updateWith(
      doors: Map[DoorPosition, DoorState],
      toAddDoor: (DoorPosition, DoorState)
  ): Map[DoorPosition, DoorState] = doors + toAddDoor

  /**
   * @param doors
   *   the doors to update
   * @param state
   *   the state to update the door with
   * @return
   *   a new map with the new state
   */
  def updateState(doors: Map[DoorPosition, DoorState])(state: DoorState): Map[DoorPosition, DoorState] =
    doors.map(d => d._1 -> state)
}

/**
 * extension to map and tuple that enable Door method calling
 */
object DoorImplicit {
  extension (doors: Map[DoorPosition, DoorState]) {
    def :+(toAddDoor: (DoorPosition, DoorState)) = updateWith(doors, toAddDoor)
    def open: Map[DoorPosition, DoorState]       = Door.updateState(doors)(Open)
    def close: Map[DoorPosition, DoorState]      = Door.updateState(doors)(Close)
    def lock: Map[DoorPosition, DoorState]       = Door.updateState(doors)(Lock)
  }
  extension (door: (DoorPosition, DoorState))
    def :+(toAddDoor: (DoorPosition, DoorState)) = updateWith(Map(door), toAddDoor)

}
