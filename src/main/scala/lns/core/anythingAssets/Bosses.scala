package lns.core.anythingAssets

import indigo.*
import lns.core.Assets

trait LokiAsset extends AnythingAsset {
  override val name: Option[String] = Some("loki")
  override val width: Int           = 41
  override val height: Int          = 47
  override val offsetY: Int         = 18
  override val scale: Double        = 5
}

/**
 * Bosses assets for loading
 */
object Bosses {
  val assets: Set[AssetType] = Set(
    AssetType.Image(AssetName("loki"), AssetPath(Assets.baseUrl + "bosses/loki.png"))
  )
}
