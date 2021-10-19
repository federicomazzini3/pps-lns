package lns.scenes.game.room.door

import lns.scenes.game.room.door.Door.updateWith

/**
 * A model for the position of the door in a room
 */
enum DoorPosition:
  case Left, Right, Above, Below

/**
 * A model for the state of the door in a room
 */
enum DoorState:
  case Open, Close, Lock

/**
 * Object that describe a door
 */
object Door {

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
  import lns.scenes.game.room.door.DoorPosition.*
  import lns.scenes.game.room.door.DoorState.*

  extension (doors: Map[DoorPosition, DoorState]) {
    def :+(toAddDoor: (DoorPosition, DoorState)) = updateWith(doors, toAddDoor)
    def open: Map[DoorPosition, DoorState]       = Door.updateState(doors)(Open)
    def close: Map[DoorPosition, DoorState]      = Door.updateState(doors)(Close)
    def lock: Map[DoorPosition, DoorState]       = Door.updateState(doors)(Lock)
  }
  extension (door: (DoorPosition, DoorState))
    def :+(toAddDoor: (DoorPosition, DoorState)) = updateWith(Map(door), toAddDoor)
}
