package lns.scenes.game.character

import lns.StartupData
import lns.scenes.game.anything.FireViewModel
import lns.scenes.game.anything.FireState
import lns.core.Animations.*
import lns.core.Macros.copyMacro

case class CharacterViewModel(
    fireAnimationTimer: Double = 0,
    fireState: FireState = FireState.NO_FIRE
) extends FireViewModel {
  type ViewModel = CharacterViewModel

  def withFireTimer(fireAnimationTimer: Double, fireState: FireState): ViewModel = copyMacro
}

object CharacterViewModel {
  def initial(startupData: StartupData): CharacterViewModel = CharacterViewModel()
}
