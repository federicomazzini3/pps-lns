package lns.core.anythingAssets

import indigo.*
import lns.core.Assets

class AltarAsset extends AnythingAsset {
  override val name: String  = "altar"
  override val width: Int    = 27
  override val height: Int   = 23
  override val offsetY: Int  = 0
  override val scale: Double = 5
}

class ArrowAsset extends AnythingAsset {
  override val name: String  = "arrow"
  override val width: Int    = 32
  override val height: Int   = 32
  override val offsetY: Int  = 0
  override val scale: Double = 5
}

object Items {
  val assets: Set[AssetType] = Set(
    AssetType.Image(AssetName("altar"), AssetPath(Assets.baseUrl + "items/altar.png")),
    AssetType.Image(AssetName("arrow"), AssetPath(Assets.baseUrl + "items/arrow.png"))
  )
}
