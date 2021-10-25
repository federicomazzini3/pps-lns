package lns.scenes.game.dungeon

import indigo.shared.datatypes.Rectangle
import lns.StartupData
import lns.scenes.game.room.RoomModel
import lns.scenes.game.room.door.{ Location, DoorState }

object Generator {

  def apply(startupData: StartupData)(grid: BasicGrid): DungeonModel =
    DungeonModel(
      grid.content
        .map((position, roomType) => (position, generateRoom(startupData: StartupData)(grid, position, roomType)))
    )

  def generateRoom(startupData: StartupData)(grid: BasicGrid, position: Position, roomType: RoomType): RoomModel =
    roomType match {
      case RoomType.Empty =>
        RoomModel.emptyRoom(startupData, position, generateDoors(grid, position))
      case RoomType.Item =>
        RoomModel.itemRoom(startupData, position, generateDoors(grid, position), null)
      case RoomType.Arena =>
        RoomModel.arenaRoom(startupData, position, generateDoors(grid, position), null, null)
      case RoomType.Boss =>
        RoomModel.bossRoom(startupData, position, generateDoors(grid, position), null)
    }

  def generateDoors(grid: BasicGrid, position: Position): Set[Location] =
    Set(Location.Left, Location.Right, Location.Above, Location.Below)
      .filter(location => Grid.near(grid)(position)(location).isDefined)
}
