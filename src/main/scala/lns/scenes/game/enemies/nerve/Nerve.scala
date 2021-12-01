package lns.scenes.game.enemies.nerve

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Animations.Nerve
import lns.core.anythingAssets.NerveAsset

/**
 * Nerve enemy view elements builder
 */
trait Nerve extends NerveAsset {

  val bodySprite: Sprite[Material.Bitmap] = spriteAnimation("nerve_body")

  /**
   * Builds the body view
   * @param model
   *   the [[NerveModel]]
   * @return
   *   body view Sprite
   */
  def bodyView(model: NerveModel): Sprite[Material.Bitmap] = bodyAnimation(model)
  //.withRef(Nerve.bodyWidth / 2, 0)
  // .moveTo(width / 2, 20)

  /**
   * Plays the animation cycle if the character is moving
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   the updated body Sprite
   */
  def bodyAnimation(model: NerveModel): Sprite[Material.Bitmap] = bodySprite.changeCycle(CycleLabel("idle")).play()

}
