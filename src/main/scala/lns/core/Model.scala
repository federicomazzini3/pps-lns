package lns.core

import lns.scenes.menu.MenuModel
import lns.scenes.loading.LoadingModel
import lns.scenes.game.GameModel
import lns.scenes.end.EndModel

/**
 * Main model which stores the whole game state divided by scene
 * @param menu [[MenuModel]] for the [[MenuScene]]
 * @param loading [[LoadingModel]] for the [[LoadingScene]]
 * @param game [[GameModel]] for the [[GameScene]]
 * @param end [[EndModel]] for the [[EndScene]]
 */
final case class Model(
                        menu: MenuModel,
                        loading: LoadingModel,
                        game: GameModel,
                        end: EndModel
                      )
object Model {

  def initial: Model =
    Model(
      MenuModel.initial,
      LoadingModel.initial,
      GameModel.initial,
      EndModel.initial
    )
}
