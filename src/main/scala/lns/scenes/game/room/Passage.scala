package lns.scenes.game.room

import lns.scenes.game.character.CharacterModel
import lns.scenes.game.dungeon.DungeonModel
import lns.scenes.game.dungeon.DungeonModel.*
import lns.scenes.game.room.door.Location.*
import lns.scenes.game.room.door.DoorState.*
import lns.scenes.game.dungeon.room

object Passage {

  def verifyPassage(dungeon: DungeonModel, room: RoomModel, character: CharacterModel): (RoomModel, CharacterModel) =
    val characterPosition = character.boundingBox

    if (
      room.floor.right == character.boundingBox.right &&
      character.boundingBox.y < 300 &&
      character.boundingBox.y > 200 &&
      room.doors.contains(Right) &&
      room.doors(Right) == Open
    )
      (
        dungeon.room(room.positionInDungeon)(Right).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(-room.floor.width + character.boundingBox.width, 0))
      )
    else if (
      room.floor.left == character.boundingBox.left &&
      character.boundingBox.y < 300 &&
      character.boundingBox.y > 200 &&
      room.doors.contains(Left) &&
      room.doors(Left) == Open
    )
      (
        dungeon.room(room.positionInDungeon)(Left).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(room.floor.width - character.boundingBox.width, 0))
      )
    else if (
      room.floor.top == character.boundingBox.top &&
      character.boundingBox.x < 300 &&
      character.boundingBox.x > 200 &&
      room.doors.contains(Above) &&
      room.doors(Above) == Open
    )
      (
        dungeon.room(room.positionInDungeon)(Above).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(0, room.floor.height - character.boundingBox.height))
      )
    else if (
      room.floor.bottom == character.boundingBox.bottom &&
      character.boundingBox.x > 200 &&
      character.boundingBox.x < 1800 &&
      room.doors.contains(Below) &&
      room.doors(Below) == Open
    )
      (
        dungeon.room(room.positionInDungeon)(Below).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(0, -room.floor.height + character.boundingBox.height))
      )
    else (room, character)
}
