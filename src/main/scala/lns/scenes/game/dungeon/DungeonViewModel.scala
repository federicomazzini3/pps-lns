package lns.scenes.game.dungeon

import lns.scenes.game.room.{ RoomModel, RoomViewModel }

/**
 * Case class of the entire Dungeon. It stores a Map that contains all the room and their position
 */
case class DungeonViewModel(val content: Map[Position, RoomViewModel], val initialRoom: Position) extends Grid {
  override type Room = RoomViewModel
}

object DungeonViewModel {
  def initial(model: DungeonModel): DungeonViewModel = DungeonViewModel(
    model.content
      .map((position, room) => (position, RoomViewModel.initial(room))),
    model.initialRoom
  )
}
