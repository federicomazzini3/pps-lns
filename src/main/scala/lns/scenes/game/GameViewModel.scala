package lns.scenes.game

import lns.StartupData
import lns.scenes.game.character.CharacterViewModel

case class GameViewModel(character: CharacterViewModel)

object GameViewModel {

  def initial(startupData: StartupData): GameViewModel = GameViewModel(CharacterViewModel.initial(startupData))

}
