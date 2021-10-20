package lns.scenes.game.dungeon

import indigo.shared.datatypes.Rectangle
import lns.StartupData
import lns.scenes.game.room.RoomModel
import lns.scenes.game.room.door.{ Location, DoorState }

object Generator {

  def apply(startupData: StartupData)(grid: BasicGrid): DungeonModel =
    DungeonModel(
      grid.row,
      grid.column,
      grid.content
        .map((position, roomType) => (position, generateRoom(startupData: StartupData)(grid)(position)))
    )

  def generateRoom(startupData: StartupData)(grid: BasicGrid)(position: Position): RoomModel =
    grid.content(position) match {
      case RoomType.Empty =>
        RoomModel.emptyRoom(startupData, position, generateDoors(grid, position)(DoorState.Open))
      case RoomType.Item =>
        RoomModel.itemRoom(startupData, position, generateDoors(grid, position)(DoorState.Open), null)
      case RoomType.Arena =>
        RoomModel.arenaRoom(startupData, position, generateDoors(grid, position)(DoorState.Close), null, null)
      case RoomType.Boss =>
        RoomModel.bossRoom(startupData, position, generateDoors(grid, position)(DoorState.Close), null)
    }

  def generateDoors(grid: BasicGrid, position: Position)(doorState: DoorState): Map[Location, DoorState] =
    Map(
      ((Location.Left)  -> doorState),
      ((Location.Right) -> doorState),
      ((Location.Above) -> doorState),
      ((Location.Below) -> doorState)
    )
      .filter((location, state) => Grid.near(grid)(position)(location).isDefined)
}
