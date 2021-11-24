package lns.core

import lns.StartupData
import lns.core.Model
import lns.scenes.end.EndViewModel
import lns.scenes.game.*
import lns.scenes.menu.MenuViewModel

/**
 * Main viewModel which stores the whole presentation state that is inessential to the game, but improves visual
 * experience.
 */
final case class ViewModel(menu: MenuViewModel, game: GameViewModel, end: EndViewModel)

object ViewModel {

  def initial(startupData: StartupData, model: Model): ViewModel =
    ViewModel(
      MenuViewModel.initial(startupData),
      GameViewModel.initial(startupData, model.game),
      EndViewModel.initial(startupData)
    )
}
