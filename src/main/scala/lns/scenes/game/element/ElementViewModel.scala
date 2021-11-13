package lns.scenes.game.element

import lns.scenes.game.anything.{ AnythingModel, AnythingViewModel, SolidModel }

trait ElementViewModel extends AnythingViewModel[SolidModel] {
  type ViewModel = ElementViewModel
}
