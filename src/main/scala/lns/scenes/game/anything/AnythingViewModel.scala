package lns.scenes.game.anything

trait AnythingViewModel {
  type ViewModel >: this.type <: AnythingViewModel
}
