package lns.scenes.game.updater

import indigoextras.geometry.Vertex
import lns.scenes.game.anything.DynamicState.{ MOVE_DOWN, MOVE_LEFT, MOVE_RIGHT, MOVE_UP }
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.dungeon.DungeonModel
import lns.scenes.game.room.*
import lns.scenes.game.room.door.Location.{ Above, Below, Left, Right }
import lns.scenes.game.room.door.{ Door, Location }
import lns.scenes.game.room.door.LocationImplicit.*

object PassageUpdater {

  /**
   * Verifies the character's intention to pass through a door and modifies the model accordingly
   * @param dungeon
   * @param room
   * @param character
   * @return
   *   The destination room and the character moved in the right position (near the destination door)
   */
  def apply(dungeon: DungeonModel, room: RoomModel, character: CharacterModel): ((Int, Int), CharacterModel) = {

    /**
     * Change the current room based on location
     * @param locations
     * @return
     *   The new dungeon position of the current room of the game
     */
    def changeRoom(locations: Location): (Int, Int) =
      dungeon.nearPosition(room.positionInDungeon)(locations).getOrElse(room.positionInDungeon)

    /**
     * Moving the character accordingly to the destination port
     * @param location
     * @return
     *   A new character with updated position
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
     * Check if the character it's aligned on a door
     * @param location
     *   the specific side to check
     * @return
     *   if the character it's aligned on a door
     */
    def characterOnDoor(location: Location): Boolean =
      val container = room.floor
      val elem      = character.boundingBox
      location match {
        case Left | Right  => container.verticalCenter > elem.top && container.verticalCenter < elem.bottom
        case Above | Below => container.horizontalCenter > elem.left && container.horizontalCenter < elem.right
      }

    /**
     * Verifies the character's intention to pass through a door and specify which
     * @return
     *   the location of the door to pass
     */
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
      case _ => (room.positionInDungeon, character)
    }
  }
}
