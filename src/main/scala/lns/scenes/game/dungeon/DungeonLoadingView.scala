package lns.scenes.game.dungeon

import indigo.shared.scenegraph.{ SceneUpdateFragment, Text }
import lns.StartupData
import lns.core.Assets

object DungeonLoadingView {
  def apply(startupData: StartupData): SceneUpdateFragment =
    SceneUpdateFragment(
      Text(
        "Dungeon loading...",
        startupData.screenDimensions.horizontalCenter,
        startupData.screenDimensions.verticalCenter,
        1,
        Assets.Fonts.fontKey,
        Assets.Fonts.fontMaterial
      ).alignCenter
    )
}
