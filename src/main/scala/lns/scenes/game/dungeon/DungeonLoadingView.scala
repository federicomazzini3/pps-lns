package lns.scenes.game.dungeon

import indigo.shared.scenegraph.{ SceneUpdateFragment, Text }
import lns.StartupData
import lns.core.Assets

object DungeonLoadingView {
  def apply(startupData: StartupData): SceneUpdateFragment = show(startupData, "Dungeon loading...")
  def apply(startupData: StartupData, dungeon: DungeonModel): SceneUpdateFragment = show(
    startupData,
    "Dungeon loading... " + dungeon.content.collect {
      case (_, room) if room.generated => room
    }.size + "/" + dungeon.content.size
  )

  def show(startupData: StartupData, message: String): SceneUpdateFragment = SceneUpdateFragment(
    Text(
      message,
      startupData.screenDimensions.horizontalCenter,
      startupData.screenDimensions.verticalCenter,
      1,
      Assets.Fonts.fontKey,
      Assets.Fonts.fontMaterial
    ).alignCenter
  )
}
