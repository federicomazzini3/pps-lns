package lns.core

import lns.scenes.menu.MenuViewModel

/**
 * Main viewModel which stores the whole presentation state
 * that is inessential to the game, but improves visual experience.
 */
final case class ViewModel(menu:MenuViewModel)

object ViewModel {

  def initial: ViewModel =
    ViewModel(MenuViewModel.initial)
}
