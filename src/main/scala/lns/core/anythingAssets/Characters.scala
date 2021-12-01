package lns.core.anythingAssets

import indigo.*
import lns.core.Assets

trait IsaacAsset extends AnythingAsset {
  override val name: Option[String] = Some("isaac")
  override val width: Int           = 28
  override val height: Int          = 33
  override val offsetY: Int         = 18
  override val scale: Double        = 5
}

/**
 * Characters assets for loading
 */
object Characters {
  val assets: Set[AssetType] = Set(
    AssetType.Image(AssetName("isaac"), AssetPath(Assets.baseUrl + "characters/isaac.png"))
  )
}
