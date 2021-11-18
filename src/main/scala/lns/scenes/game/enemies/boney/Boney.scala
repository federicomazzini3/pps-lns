package lns.scenes.game.enemies.boney

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Animations.Boney
import lns.core.anythingAssets.BoneyAsset
import lns.scenes.game.anything.DynamicState

/**
 * Boonie enemy view elements builder
 */
trait Boney extends BoneyAsset {

  val bodySprite: Sprite[Material.Bitmap] = spriteAnimation("boney_body")

  /**
   * Builds the head view
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   head view Graphic
   */
  def headView(model: BoneyModel): Graphic[Material.Bitmap] =
    Graphic(Boney.headCrop(model.getDynamicState()), 1, Material.Bitmap(asset))
      .withRef(0, 0)
      .flipHorizontal(toFlip(model))
      .moveTo(0, 0)

  /**
   * Builds the body view
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   body view Sprite
   */
  def bodyView(model: BoneyModel): Sprite[Material.Bitmap] =
    bodyAnimation(model)
      .withRef(Boney.bodyWidth / 2, 0)
      .flipHorizontal(toFlip(model))
      .moveTo(width / 2, 20)

  /**
   * An asset image may be represented only in right position instead of left + right. In this case we need to flip it
   * if the model is moving leftward
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   true if image need to be flipped to represent current dynamic state.
   */
  def toFlip(model: BoneyModel): Boolean = model.getDynamicState() match {
    case DynamicState.MOVE_LEFT => true
    case _                      => false
  }

  /**
   * Plays the animation cycle if the character is moving
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   the updated body Sprite
   */
  def bodyAnimation(model: BoneyModel): Sprite[Material.Bitmap] = model.isMoving() match {
    case true => bodySprite.changeCycle(bodyAnimationCycle(model)).play()
    case _    => bodySprite.changeCycle(bodyAnimationCycle(model)).jumpToFirstFrame()
  }

  /**
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   the animation CycleLabel for the body to be played based on current dynamic state
   */
  def bodyAnimationCycle(model: BoneyModel): CycleLabel = model.getDynamicState() match {
    case DynamicState.MOVE_LEFT | DynamicState.MOVE_RIGHT => CycleLabel("walking_left_right")
    case _                                                => CycleLabel("walking_up_down")
  }

}
