package lns.scenes.game.dungeon

import indigo.shared.datatypes.Rectangle
import lns.StartupData
import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.room.RoomModel
import lns.scenes.game.room.door.{ DoorState, Location }

object Generator {

  def apply(grid: BasicGrid): DungeonModel =
    DungeonModel(
      grid.content
        .map((position, roomType) => (position, generateRoom(grid, position, roomType)))
    )

  def generateRoom(grid: BasicGrid, position: Position, roomType: RoomType): RoomModel =
    roomType match {
      case RoomType.Empty =>
        RoomModel.emptyRoom(position, generateDoors(grid, position))
      case RoomType.Item =>
        RoomModel.itemRoom(position, generateDoors(grid, position), null)
      case RoomType.Arena =>
        RoomModel.arenaRoom(position, generateDoors(grid, position), Set.empty[AnythingModel], Set.empty[AnythingModel])
      case RoomType.Boss =>
        RoomModel.bossRoom(position, generateDoors(grid, position), null)
    }

  def generateDoors(grid: BasicGrid, position: Position): Set[Location] =
    Set(Location.Left, Location.Right, Location.Above, Location.Below)
      .filter(location => Grid.near(grid)(position)(location).isDefined)
}
