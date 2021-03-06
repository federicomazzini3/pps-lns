package lns.core.anythingAssets

import indigo.*
import lns.core.Assets

trait BoneyAsset extends AnythingAsset {
  override val name: Option[String] = Some("boney")
  override val width: Int           = 28
  override val height: Int          = 33
  override val offsetY: Int         = 13
  override val scale: Double        = 5
}

trait MaskAsset extends AnythingAsset {
  override val name: Option[String] = Some("mask")
  override val width: Int           = 28
  override val height: Int          = 33
  override val offsetY: Int         = 0
  override val scale: Double        = 5
}

trait NerveAsset extends AnythingAsset {
  override val name: Option[String] = Some("nerve")
  override val width: Int           = 26
  override val height: Int          = 50
  override val offsetY: Int         = 30
  override val scale: Double        = 5
}

trait ParabiteAsset extends AnythingAsset {
  override val name: Option[String] = Some("parabite")
  override val width: Int           = 26
  override val height: Int          = 33
  override val offsetY: Int         = 13
  override val scale: Double        = 5
}

/**
 * Enemies assets for loading
 */
object Enemies {
  val assets: Set[AssetType] = Set(
    AssetType.Image(AssetName("boney"), AssetPath(Assets.baseUrl + "enemies/boney.png")),
    AssetType.Image(AssetName("mask"), AssetPath(Assets.baseUrl + "enemies/mask.png")),
    AssetType.Image(AssetName("nerve"), AssetPath(Assets.baseUrl + "enemies/nerve.png")),
    AssetType.Image(AssetName("parabite"), AssetPath(Assets.baseUrl + "enemies/parabite.png"))
  )
}
