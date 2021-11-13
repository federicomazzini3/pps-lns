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

type Door           = (Location, DoorState)
type DoorsLocations = Set[Location]
type Doors          = Map[Location, DoorState]

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
  def apply(door: Door) = Map(door)

  /**
   * @param doors
   *   the map of doors to update
   * @param toAddDoor
   *   the door to add
   * @return
   *   a new map with the door added
   */
  def updateWith(
      doors: Doors,
      toAddDoor: Door
  ): Doors = doors + toAddDoor

  /**
   * @param doors
   *   the doors to update
   * @param state
   *   the state to update the door with
   * @return
   *   a new map with the new state
   */
  def updateState(doors: Doors)(state: DoorState): Doors =
    doors.map(d => d._1 -> state)

  /**
   * Verify if a door is open
   * @param doors
   *   the entire set of doors
   * @param location
   *   the specif location of the door to verify
   * @return
   *   true if the door is open
   */
  def verifyOpen(doors: Doors)(location: Location): Boolean =
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

  extension (doors: Doors) {
    def :+(toAddDoor: Door) = updateWith(doors, toAddDoor)
    def open: Doors         = Door.updateState(doors)(Open)
    def close: Doors        = Door.updateState(doors)(Close)
    def lock: Doors         = Door.updateState(doors)(Lock)
  }
  extension (door: Door) {
    def :+(toAddDoor: Door) = updateWith(Map(door), toAddDoor)
  }

  extension (doorsLocations: Set[Location]) {
    def :+(toAddDoor: Location): Set[Location] = doorsLocations + toAddDoor
    def open: Doors                            = doorsLocations.map(loc => loc -> DoorState.Open).toMap
    def close: Doors                           = doorsLocations.map(loc => loc -> DoorState.Close).toMap
    def lock: Doors                            = doorsLocations.map(loc => loc -> DoorState.Lock).toMap
  }

  extension (doorLocation: Location) {
    def :+(toAddLocation: Location): Set[Location] = Set(doorLocation, toAddLocation)
  }
}

object LocationImplicit {
  import Location.*
  extension (location: Location) {
    def opposite: Location = location match {
      case Above => Below
      case Below => Above
      case Right => Left
      case Left  => Right
    }
  }
}
