package lns.scenes.game.updater

import lns.scenes.game.dungeon.DungeonModel
import lns.scenes.game.room.*
import door.Location.*
import indigo.shared.datatypes.Vector2
import indigoextras.geometry.Vertex
import lns.scenes.game.characters.CharacterModel
import org.scalatest.freespec.AnyFreeSpec

class PassageUpdaterTest extends AnyFreeSpec {

  val initialRoom = RoomModel.emptyRoom((1, 1), Set(Left, Right, Above, Below))
  val leftRoom    = RoomModel.emptyRoom((0, 1), Set(Right))
  val rightRoom   = RoomModel.emptyRoom((2, 1), Set(Left))
  val aboveRoom   = RoomModel.emptyRoom((1, 2), Set(Below))
  val belowRoom   = RoomModel.emptyRoom((1, 0), Set(Above))

  val character = CharacterModel.initial

  val dungeon: DungeonModel = DungeonModel(
    Map(
      initialRoom.positionInDungeon -> initialRoom,
      leftRoom.positionInDungeon    -> leftRoom,
      rightRoom.positionInDungeon   -> rightRoom,
      aboveRoom.positionInDungeon   -> aboveRoom,
      belowRoom.positionInDungeon   -> belowRoom
    ),
    initialRoom.positionInDungeon
  )

  object Speed {
    val toLeft  = Vector2(-1, 0)
    val toRight = Vector2(1, 0)
    val toAbove = Vector2(0, -1)
    val toBelow = Vector2(0, 1)
  }

  object DoorPosition {
    val roomSize = 1375
    val left     = Vertex(0, roomSize / 2)
    val right    = Vertex(roomSize, roomSize / 2)
    val above    = Vertex(roomSize / 2, 0)
    val below    = Vertex(roomSize / 2, roomSize)
  }

  "Inside a dungeon" - {
    "composed of room" - {
      "a character not adjacent to a door" - {
        "should not pass to the room on the other side" in {
          val newRoomPosition = PassageUpdater(dungeon, initialRoom, character)._1
          assert(newRoomPosition == initialRoom.positionInDungeon)
        }
      }
      "a character adjacent to a left door" - {
        val movedCharacter = character
          .withDynamic(character.boundingBox.moveTo(DoorPosition.left), Speed.toLeft)
          .asInstanceOf[CharacterModel]
        "should pass to the room adjacent to the current room on left side" in {
          val newCharacter = PassageUpdater(dungeon, initialRoom, movedCharacter)._2
          assert(movedCharacter.boundingBox.position != newCharacter.boundingBox.position)
        }
        "the left room becomes the new current room" in {
          val newRoomPosition = PassageUpdater(dungeon, initialRoom, movedCharacter)._1
          assert(newRoomPosition == leftRoom.positionInDungeon)
        }
      }
      "a character adjacent to a right door" - {
        val movedCharacter = character
          .withDynamic(character.boundingBox.moveTo(DoorPosition.right), Speed.toRight)
          .asInstanceOf[CharacterModel]
        "should pass to the room adjacent to the current room on right side" in {
          val newCharacter = PassageUpdater(dungeon, initialRoom, movedCharacter)._2
          assert(movedCharacter.boundingBox.position != newCharacter.boundingBox.position)
        }
        "the right room becomes the new current room" in {
          val newRoomPosition = PassageUpdater(dungeon, initialRoom, movedCharacter)._1
          assert(newRoomPosition == rightRoom.positionInDungeon)
        }
      }
      "a character adjacent to the door above" - {
        val movedCharacter = character
          .withDynamic(character.boundingBox.moveTo(DoorPosition.above), Speed.toAbove)
          .asInstanceOf[CharacterModel]
        "should pass to the room adjacent to the current room on above side" in {
          val newCharacter = PassageUpdater(dungeon, initialRoom, movedCharacter)._2
          assert(movedCharacter.boundingBox.position != newCharacter.boundingBox.position)
        }
        "the above room becomes the new current room" in {
          val newRoomPosition = PassageUpdater(dungeon, initialRoom, movedCharacter)._1
          assert(newRoomPosition == aboveRoom.positionInDungeon)
        }
      }
      "a character adjacent to the door below" - {
        val movedCharacter = character
          .withDynamic(character.boundingBox.moveTo(DoorPosition.below), Speed.toBelow)
          .asInstanceOf[CharacterModel]
        "should pass to the room adjacent to the current room on below side" in {
          val newCharacter = PassageUpdater(dungeon, initialRoom, movedCharacter)._2
          assert(movedCharacter.boundingBox.position != newCharacter.boundingBox.position)
        }
        "the below room becomes the new current room" in {
          val newRoomPosition = PassageUpdater(dungeon, initialRoom, movedCharacter)._1
          assert(newRoomPosition == belowRoom.positionInDungeon)
        }
      }
    }
  }

}
