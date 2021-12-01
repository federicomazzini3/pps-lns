package lns.scenes.game.characters

import lns.StartupData
import lns.scenes.game.anything.{ AnythingId, Timer, FireViewModel, FireState }
import lns.core.Animations.*
import lns.core.Macros.copyMacro

/**
 * Character ViewModel to manage its animations
 */
case class CharacterViewModel(
    id: AnythingId,
    fireAnimationTimer: Timer = 0,
    fireState: FireState = FireState.NO_FIRE
) extends FireViewModel[CharacterModel] {
  type ViewModel = CharacterViewModel

  def withFireTimer(fireAnimationTimer: Timer, fireState: FireState): ViewModel = copyMacro
}

/**
 * Factory of [[CharacterViewModel]]
 */
object CharacterViewModel {
  def initial(id: AnythingId): CharacterViewModel = CharacterViewModel(id)
}
