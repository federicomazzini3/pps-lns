package lns.scenes.game.room

import lns.scenes.game.character.CharacterModel
import lns.scenes.game.dungeon.DungeonModel
import lns.scenes.game.dungeon.DungeonModel.*
import lns.scenes.game.room.door.Location.*
import lns.scenes.game.room.door.DoorState.*
import lns.scenes.game.dungeon.room

object Passage {

  def currentRoom(dungeon: DungeonModel, room: RoomModel, character: CharacterModel): (RoomModel, CharacterModel) =
    val characterPosition = character.boundingBox

    if (
      Boundary.onBound(room.floor, characterPosition)(Right) && room.doors.contains(Right) && room.doors(Right) == Open
    )
      (
        dungeon.room(room.positionInDungeon)(Right).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(-room.floor.width + character.boundingBox.width, 0))
      )
    else if (
      Boundary.onBound(room.floor, characterPosition)(Left) && room.doors.contains(Left) &&
      room.doors(Left) == Open
    )
      (
        dungeon.room(room.positionInDungeon)(Left).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(room.floor.width - character.boundingBox.width, 0))
      )
    else if (
      Boundary.onBound(room.floor, characterPosition)(Above) && room.doors.contains(Above) &&
      room.doors(Above) == Open
    )
      (
        dungeon.room(room.positionInDungeon)(Above).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(0, room.floor.height - character.boundingBox.height))
      )
    else if (
      Boundary.onBound(room.floor, characterPosition)(Below) && room.doors.contains(Below) &&
      room.doors(Below) == Open
    )
      (
        dungeon.room(room.positionInDungeon)(Below).getOrElse(room),
        character.copy(boundingBox = character.boundingBox.moveBy(0, -room.floor.height + character.boundingBox.height))
      )
    else (room, character)
}
