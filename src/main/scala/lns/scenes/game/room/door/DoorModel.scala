package lns.scenes.game.room.door

import lns.scenes.game.room.door.Door.updateWith

/**
 * A model for the position of the door in a room
 */
enum Location:
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
  def apply(door: (Location, DoorState)) = Map(door)

  /**
   * @param doors
   *   the map of doors to update
   * @param toAddDoor
   *   the door to add
   * @return
   *   a new map with the door added
   */
  def updateWith(
      doors: Map[Location, DoorState],
      toAddDoor: (Location, DoorState)
  ): Map[Location, DoorState] = doors + toAddDoor

  /**
   * @param doors
   *   the doors to update
   * @param state
   *   the state to update the door with
   * @return
   *   a new map with the new state
   */
  def updateState(doors: Map[Location, DoorState])(state: DoorState): Map[Location, DoorState] =
    doors.map(d => d._1 -> state)
}

/**
 * extension to map and tuple that enable Door method calling
 */
object DoorImplicit {
  import lns.scenes.game.room.door.Location.*
  import lns.scenes.game.room.door.DoorState.*

  extension (doors: Map[Location, DoorState]) {
    def :+(toAddDoor: (Location, DoorState)) = updateWith(doors, toAddDoor)
    def open: Map[Location, DoorState]       = Door.updateState(doors)(Open)
    def close: Map[Location, DoorState]      = Door.updateState(doors)(Close)
    def lock: Map[Location, DoorState]       = Door.updateState(doors)(Lock)
  }
  extension (door: (Location, DoorState)) def :+(toAddDoor: (Location, DoorState)) = updateWith(Map(door), toAddDoor)
}
