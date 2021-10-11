package lns.core

import lns.StartupData
import lns.scenes.loading.LoadingModel
import lns.scenes.game.GameModel
import lns.scenes.end.EndModel

/**
 * Main model which stores the whole game state divided by scene
 * @param loading
 *   [[LoadingModel]] for the [[LoadingScene]]
 * @param game
 *   [[GameModel]] for the [[GameScene]]
 * @param end
 *   [[EndModel]] for the [[EndScene]]
 */
final case class Model(
    loading: LoadingModel,
    game: GameModel,
    end: EndModel
)
object Model {

  def initial(startupData: StartupData): Model =
    Model(
      LoadingModel.initial,
      GameModel.initial(startupData),
      EndModel.initial
    )
}
