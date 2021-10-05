package lns.core

/**
 * Main viewModel which stores the whole presentation state
 * that is inessential to the game, but improves visual experience.
 */
final case class ViewModel()

object ViewModel {

  def initial: ViewModel =
    ViewModel()
}
