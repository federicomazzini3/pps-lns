package lns.scenes.game.room

import lns.scenes.game.character.CharacterModel
import lns.scenes.game.anything.*
import lns.scenes.game.dungeon.DungeonModel
import lns.scenes.game.room.door.Door
import lns.scenes.game.dungeon.DungeonModel.*
import lns.scenes.game.room.door.Location.*
import lns.scenes.game.room.door.DoorState.*
import lns.scenes.game.dungeon.room

object Passage {

  def verifyPassage(dungeon: DungeonModel, room: RoomModel, character: CharacterModel): (RoomModel, CharacterModel) =
    if (
      room.floor.right == character.boundingBox.right &&
      room.floor.verticalCenter > character.boundingBox.top &&
      room.floor.verticalCenter < character.boundingBox.bottom &&
      Door.verifyOpen(room.doors)(Right) &&
      character.getState() == DynamicState.MOVE_RIGHT
    )
      (
        dungeon.room(room.positionInDungeon)(Right).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(-room.floor.width + character.boundingBox.width, 0))
      )
    else if (
      room.floor.left == character.boundingBox.left &&
      room.floor.verticalCenter > character.boundingBox.top &&
      room.floor.verticalCenter < character.boundingBox.bottom &&
      Door.verifyOpen(room.doors)(Left) &&
      character.getState() == DynamicState.MOVE_LEFT
    )
      (
        dungeon.room(room.positionInDungeon)(Left).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(room.floor.width - character.boundingBox.width, 0))
      )
    else if (
      room.floor.top == character.boundingBox.bottom &
        room.floor.horizontalCenter > character.boundingBox.bottomLeft.x &&
        room.floor.horizontalCenter < character.boundingBox.bottomRight.x &&
        Door.verifyOpen(room.doors)(Above) &&
        character.getState() == DynamicState.MOVE_UP
    )
      (
        dungeon.room(room.positionInDungeon)(Above).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(0, room.floor.height - character.boundingBox.height))
      )
    else if (
      room.floor.bottom == character.boundingBox.bottom &&
      room.floor.horizontalCenter > character.boundingBox.bottomLeft.x &&
      room.floor.horizontalCenter < character.boundingBox.bottomRight.x &&
      Door.verifyOpen(room.doors)(Below) &&
      character.getState() == DynamicState.MOVE_DOWN
    )
      (
        dungeon.room(room.positionInDungeon)(Below).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(0, -room.floor.height + character.boundingBox.height))
      )
    else (room, character)
}
