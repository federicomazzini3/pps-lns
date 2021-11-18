package lns.scenes.game.enemies.mask

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Animations.Mask
import lns.core.anythingAssets.MaskAsset
import lns.scenes.game.anything.{ DynamicState, FireState }

/**
 * Mask enemy view elements builder
 */
trait Mask extends MaskAsset {

  /**
   * Builds the head view
   * @param model
   *   the [[MaskModel]]
   * @return
   *   head view Graphic
   */
  def headView(model: MaskModel): Graphic[Material.Bitmap] =
    Graphic(headDirection(model), 1, Material.Bitmap(asset))
      .withRef(0, 0)
      .moveTo(0, 0)

  /**
   * Manual Animation for the head. The right frame is selected based on the FireState and DynamicState
   * @param model
   *   the [[MaskModel]]
   * @return
   *   Cropped Rectangle of the asset
   */
  def headDirection(model: MaskModel): Rectangle = model.isFiring() match {
    case true => Mask.headCrop(model.getFireState())
    case _    => Mask.headCrop(model.getDynamicState())
  }

}
