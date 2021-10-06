package lns.scenes.loading

import indigo.*
import lns.core.Assets

object LoadingView {

  def draw(
            loadingState: LoadingState,
            screenDimensions: Rectangle
          ): SceneUpdateFragment = {

    val message: String =
      loadingState match {
        case LoadingState.NotStarted =>
          "Loading..."

        case LoadingState.InProgress(percent) =>
          s"Loading...${percent.toString()}%"

        case LoadingState.Complete =>
          "Loading...100%"

        case LoadingState.Error =>
          "Loading failed..."
      }

    SceneUpdateFragment(
      Text(
        message,
        screenDimensions.horizontalCenter,
        screenDimensions.verticalCenter,
        1,
        Assets.Fonts.fontKey,
        Assets.Fonts.fontMaterial
      )
    )
  }

}

