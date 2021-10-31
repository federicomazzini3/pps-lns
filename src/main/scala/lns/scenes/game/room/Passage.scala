package lns.scenes.game.room

import indigoextras.geometry.BoundingBox
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
    def changeRoom(dungeon: DungeonModel, room: RoomModel)(locations: Location): RoomModel = locations match {
      case Left  => dungeon.room(room.positionInDungeon)(Left).getOrElse(room)
      case Right => dungeon.room(room.positionInDungeon)(Right).getOrElse(room)
      case Above => dungeon.room(room.positionInDungeon)(Above).getOrElse(room)
      case Below => dungeon.room(room.positionInDungeon)(Below).getOrElse(room)
    }

    /**
     * moving the character accordingly to the destination port
     * @param character
     * @param floor
     * @param doorDestination
     * @return
     *   a new character with updated position
     */
    def moveCharacter(character: CharacterModel)(floor: BoundingBox)(doorDestination: Location): CharacterModel =
      doorDestination match {
        case Left =>
          character.copy(boundingBox = character.boundingBox.moveTo(floor.left, character.boundingBox.y))
        case Right =>
          character.copy(boundingBox =
            character.boundingBox.moveTo(floor.right - character.boundingBox.width, character.boundingBox.y)
          )
        case Above =>
          character.copy(boundingBox =
            character.boundingBox.moveTo(character.boundingBox.x, floor.top - character.boundingBox.height)
          )
        case Below =>
          character.copy(boundingBox =
            character.boundingBox.moveTo(character.boundingBox.x, floor.bottom - character.boundingBox.height)
          )
      }

    character.getState() match {
      case DynamicState.MOVE_RIGHT
          if Boundary.beyond(room.floor, character.boundingBox)(Right) &&
            Boundary.centerAligned(room.floor, character.boundingBox)(Right) &&
            Door.verifyOpen(room.doors)(Right) =>
        (changeRoom(dungeon, room)(Right), moveCharacter(character)(room.floor)(Left))

      case DynamicState.MOVE_LEFT
          if Boundary.beyond(room.floor, character.boundingBox)(Left) &&
            Boundary.centerAligned(room.floor, character.boundingBox)(Left) &&
            Door.verifyOpen(room.doors)(Left) =>
        (changeRoom(dungeon, room)(Left), moveCharacter(character)(room.floor)(Right))

      case DynamicState.MOVE_UP
          if Boundary.beyond(room.floor, character.boundingBox)(Above) &&
            Boundary.centerAligned(room.floor, character.boundingBox)(Above) &&
            Door.verifyOpen(room.doors)(Above) =>
        (changeRoom(dungeon, room)(Above), moveCharacter(character)(room.floor)(Below))

      case DynamicState.MOVE_DOWN
          if Boundary.beyond(room.floor, character.boundingBox)(Below) &&
            Boundary.centerAligned(room.floor, character.boundingBox)(Below) &&
            Door.verifyOpen(room.doors)(Below) =>
        (changeRoom(dungeon, room)(Below), moveCharacter(character)(room.floor)(Above))

      case _ => (room, character)
    }
  }
}
