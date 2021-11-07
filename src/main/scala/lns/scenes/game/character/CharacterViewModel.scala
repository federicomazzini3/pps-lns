package lns.scenes.game.character

import lns.StartupData
import lns.scenes.game.anything.FireViewModel
import lns.scenes.game.anything.FireState
import lns.core.Animations.*
import lns.core.Macros.copyMacro

/**
 * Character ViewModel to manage its animations
 */
case class CharacterViewModel(
    fireAnimationTimer: Double = 0,
    fireState: FireState = FireState.NO_FIRE
) extends FireViewModel {
  type ViewModel = CharacterViewModel
  type Model     = CharacterModel

  def withFireTimer(fireAnimationTimer: Double, fireState: FireState): ViewModel = copyMacro
}

object CharacterViewModel {
  def initial: CharacterViewModel = CharacterViewModel()
}
