package lns.scenes.game

import indigo._
import lns.StartupData
import lns.core._

object GameView {

  def draw(startupData: StartupData, model: GameModel, viewModel: GameViewModel): SceneUpdateFragment =
    import Assets.Rooms.EmptyRoom as Room

    SceneUpdateFragment.empty
      .addLayer(
        Layer(
          BindingKey("room"),
          Graphic(
            Rectangle(
              0,
              0,
              Room.size,
              Room.size
            ),
            1,
            Material.Bitmap(Room.name)
          )
            .withRef(Room.size / 2, Room.size / 2)
            .withScale(GameViewUtils.getScale(startupData.screenDimensions, Room.size))
            .moveTo(startupData.screenDimensions.horizontalCenter, startupData.screenDimensions.verticalCenter)
        )
      )
}

object GameViewUtils {

  /**
   * Calculate the right scale
   * @param screenDimension
   * @return
   *   the right dimension of scale for the image
   */
  def getScale(screenDimension: Rectangle, imageDimesion: Int): Vector2 =
    Vector2(Math.min(1.0 / imageDimesion * screenDimension.width, 1.0 / imageDimesion * screenDimension.height))
}
