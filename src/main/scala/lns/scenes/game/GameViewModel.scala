package lns.scenes.game

import lns.StartupData
import lns.scenes.game.characters.CharacterViewModel
import lns.scenes.game.dungeon.DungeonViewModel
import lns.scenes.game.room.RoomViewModel
import lns.scenes.game.dungeon.*

sealed trait GameViewModel

object GameViewModel {
  case class Started(val dungeon: DungeonViewModel, val room: RoomViewModel, val character: CharacterViewModel)
      extends GameViewModel
  case class NotStarted() extends GameViewModel

  def initial(startupData: StartupData, model: GameModel): GameViewModel = NotStarted()

  def start(model: GameModel.Started) = Started(
    DungeonViewModel.initial(model.dungeon),
    RoomViewModel.initial(model.currentRoom),
    CharacterViewModel.initial(model.character.id)
  )

}
