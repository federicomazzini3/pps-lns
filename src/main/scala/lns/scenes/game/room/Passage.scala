package lns.scenes.game.room

import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.scenes.game.GameModel.GameStarted
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.anything.*
import lns.scenes.game.dungeon.DungeonModel
import lns.scenes.game.room.door.Door
import lns.scenes.game.dungeon.DungeonModel.*
import lns.scenes.game.room.door.Location.*
import lns.scenes.game.room.door.Location
import lns.scenes.game.room.door.DoorState.*
import lns.scenes.game.dungeon.room
import lns.scenes.game.room.door.LocationImplicit.opposite
import DynamicState.*

object Passage {

  /**
   * verifies the character's intention to pass through a door and modifies the model accordingly
   * @param dungeon
   * @param room
   * @param character
   * @return
   *   the destination room and the character spwan in the right position (near the destination door)
   */
  def verifyPassage(dungeon: DungeonModel, room: RoomModel, character: CharacterModel): (RoomModel, CharacterModel) = {

    /**
     * change the current room based on location
     * @param dungeon
     * @param room
     * @param locations
     * @return
     *   the new current room of the game
     */
    def changeRoom(locations: Location): RoomModel =
      dungeon.room(room.positionInDungeon)(locations).getOrElse(room)

    /**
     * moving the character accordingly to the destination port
     * @param character
     * @param floor
     * @param location
     * @return
     *   a new character with updated position
     */
    def moveCharacter(location: Location): CharacterModel =
      val moves: Map[Location, Vertex] =
        Map(
          Above -> Vertex(character.boundingBox.x, room.floor.top - character.boundingBox.height),
          Below -> Vertex(character.boundingBox.x, room.floor.bottom - character.boundingBox.height),
          Left  -> Vertex(room.floor.left, character.boundingBox.y),
          Right -> Vertex(room.floor.right - character.boundingBox.width, character.boundingBox.y)
        )

      character.copy(boundingBox = character.boundingBox.moveTo(moves(location)))

    /**
     * Check, for an element, if it's center aligned in a specific edge of its container
     * @param container
     *   the element's container
     * @param elem
     *   the element inside
     * @param location
     *   the specific edge to check
     * @return
     *   if the element is center aligned
     */
    def characterOnDoor(location: Location): Boolean =
      val container = room.floor
      val elem      = character.boundingBox
      location match {
        case Left | Right  => container.verticalCenter > elem.top && container.verticalCenter < elem.bottom
        case Above | Below => container.horizontalCenter > elem.left && container.horizontalCenter < elem.right
      }

    def doorToPass: Option[Location] =
      (Collision.withContainer(room.floor, character.boundingBox), character.getDynamicState()) match {

        case ((None, Some(Above)), MOVE_UP) if Door.verifyOpen(room.doors)(Above) && characterOnDoor(Above) =>
          Some(Above)

        case ((None, Some(Below)), MOVE_DOWN) if Door.verifyOpen(room.doors)(Below) && characterOnDoor(Below) =>
          Some(Below)

        case ((Some(Left), None), MOVE_LEFT) if Door.verifyOpen(room.doors)(Left) && characterOnDoor(Left) =>
          Some(Left)

        case ((Some(Right), None), MOVE_RIGHT) if Door.verifyOpen(room.doors)(Right) && characterOnDoor(Right) =>
          Some(Right)

        case _ => None
      }

    doorToPass match {
      case Some(doorLocation) =>
        (changeRoom(doorLocation), moveCharacter(doorLocation.opposite))
      case _ => (room, character)
    }
  }
}
