package lns.scenes.game.elements.stone

import indigo.*
import indigo.shared.materials.Material
import indigo.shared.scenegraph.Graphic
import lns.core.anythingAssets.StoneAsset

/**
 * Stone view elements builder
 */
trait Stone extends StoneAsset {
  def stoneView: Graphic[Material.Bitmap] =
    Graphic(
      Rectangle(0, 0, width, height),
      1,
      Material.Bitmap(asset)
    ).withScale(Vector2(1, 1.40))
}
