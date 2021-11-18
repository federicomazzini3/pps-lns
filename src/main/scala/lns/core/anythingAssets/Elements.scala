package lns.core.anythingAssets

import indigo.*
import lns.core.Assets

trait StoneAsset extends AnythingAsset {
  override val name: String  = "stone"
  override val width: Int    = 145
  override val height: Int   = 180 //original dimension 128
  override val offsetY: Int  = 35
  override val scale: Double = 1.048
}

/**
 * Elements assets for loading
 */
object Elements {
  val assets: Set[AssetType] = Set(
    AssetType.Image(AssetName("stone"), AssetPath(Assets.baseUrl + "elements/stone.png"))
  )
}
