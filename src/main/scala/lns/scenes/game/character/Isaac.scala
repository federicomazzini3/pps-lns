package lns.scenes.game.character

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Animations.*
import lns.core.anythingAssets.IsaacAsset
import lns.scenes.game.anything.*
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.*

/**
 * Isaac Character view elements builder
 */
trait Isaac extends IsaacAsset {

  val bodySprite: Sprite[Material.Bitmap] = spriteAnimation("isaac_body")

  /**
   * Builds the head view
   *
   * @param model
   *   the [[CharacterModel]]
   * @return
   *   head view Graphic
   */
  def headView(model: CharacterModel, viewModel: CharacterViewModel): Graphic[Material.Bitmap] =
    Graphic(headAnimation(model, viewModel), 1, Material.Bitmap(asset))
      .withRef(0, 0)
      .moveTo(0, 0)

  /**
   * Manual Animation for the head. The right frame is selected based on the FireState and DynamicState
   *
   * @param model
   *   the [[CharacterModel]]
   * @param viewModel
   *   the [[CharacterViewModel]]
   * @return
   *   Cropped Rectangle of the asset
   */
  def headAnimation(model: CharacterModel, viewModel: CharacterViewModel): Rectangle =
    viewModel.fireAnimationTimer match {
      case x if x > 0 && x > FireRate @@ model.stats / 2 =>
        Isaac.headCrop(viewModel.fireState, false)
      case x if x > 0 => Isaac.headCrop(viewModel.fireState, true)
      case _ =>
        model.isFiring() match {
          case true => Isaac.headCrop(model.getFireState(), false)
          case _    => Isaac.headCrop(model.getDynamicState(), true)
        }
    }

  /**
   * Builds the body view
   * @param model
   *   the [[CharacterModel]]
   * @return
   *   body view Sprite
   */
  def bodyView(model: CharacterModel): Sprite[Material.Bitmap] =
    bodyAnimation(model)
      .withRef(Isaac.bodyWidth / 2, 0)
      .flipHorizontal(bodyFlip(model))
      .moveTo(width / 2, 20)

  /**
   * Plays the animation cycle if the character is moving
   * @param model
   *   the [[CharacterModel]]
   * @return
   *   the updated body Sprite
   */
  def bodyAnimation(model: CharacterModel): Sprite[Material.Bitmap] = model.isMoving() match {
    case true => bodySprite.changeCycle(bodyAnimationCycle(model)).play()
    case _    => bodySprite.changeCycle(bodyAnimationCycle(model)).jumpToFirstFrame()
  }

  /**
   * @param model
   *   the [[CharacterModel]]
   * @return
   *   the animation CycleLabel for the body to be played based on current dynamic state
   */
  def bodyAnimationCycle(model: CharacterModel): CycleLabel = model.getDynamicState() match {
    case DynamicState.MOVE_LEFT | DynamicState.MOVE_RIGHT => CycleLabel("walking_left_right")
    case _                                                => CycleLabel("walking_up_down")
  }

  /**
   * @param model
   *   the [[CharacterModel]]
   * @return
   *   true if body image need to be flipped to represent current dynamic state
   */
  def bodyFlip(model: CharacterModel): Boolean = model.getDynamicState() match {
    case DynamicState.MOVE_LEFT => true
    case _                      => false
  }

}
