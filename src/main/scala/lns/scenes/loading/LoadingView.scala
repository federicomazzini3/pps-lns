package lns.scenes.loading

import indigo.*
import lns.core.Assets

object LoadingView {

  def draw(
            loadingState: LoadingState
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
        100,
        110,
        1,
        Assets.Fonts.fontKey,
        Assets.Fonts.fontMaterial
      )
    )
  }

}

