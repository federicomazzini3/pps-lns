package lns.scenes.game.room.door

import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.room.door
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

  def verifyOpen(doors: Map[Location, DoorState])(location: Location): Boolean =
    doors.get(location) match {
      case Some(DoorState.Open) => true
      case _                    => false
    }
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
  extension (door: (Location, DoorState)) {
    def :+(toAddDoor: (Location, DoorState)) = updateWith(Map(door), toAddDoor)
  }

  extension (doorsLocation: Set[Location]) {
    def :+(toAddDoor: Location): Set[Location] = doorsLocation + toAddDoor
  }

  extension (doorLocation: Location) {
    def :+(toAddLocation: Location): Set[Location] = Set(doorLocation, toAddLocation)
  }
}

object InitialDoorSetup {
  private def open(locations: Set[Location]): Doors  = locations.map(loc => loc -> DoorState.Open).toMap
  private def close(locations: Set[Location]): Doors = locations.map(loc => loc -> DoorState.Close).toMap

  def empty(locations: Set[Location]): Doors = open(locations)

  def item(locations: Set[Location]): Doors = open(locations)

  def arena(locations: Set[Location])(enemies: Set[AnythingModel]): Doors = enemies.size match {
    case 0 => open(locations)
    case _ => close(locations)
  }

  def boss(locations: Set[Location]): Doors = close(locations)
}
