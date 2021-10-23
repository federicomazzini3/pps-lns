package lns.scenes.loading

import indigo.*
import lns.core.Assets

object LoadingView {

  def draw(
      screenDimensions: Rectangle,
      loadingModel: LoadingModel
  ): SceneUpdateFragment = {

    val message: String =
      loadingModel match {
        case LoadingModel.NotStarted =>
          "Loading..."

        case LoadingModel.InProgress(percent) =>
          s"Loading...${percent.toString()}%"

        case LoadingModel.Complete =>
          "Loading...100%"

        case LoadingModel.Error =>
          "Loading failed..."

        case LoadingModel.AwaitPrologConsult(_) | LoadingModel.AwaitPrologQuery(_) |
            LoadingModel.AwaitPrologAnswer(_) =>
          "Generating dungeon..."
      }

    SceneUpdateFragment(
      Text(
        message,
        screenDimensions.horizontalCenter,
        screenDimensions.verticalCenter,
        1,
        Assets.Fonts.fontKey,
        Assets.Fonts.fontMaterial
      ).alignCenter
    )
  }

}
