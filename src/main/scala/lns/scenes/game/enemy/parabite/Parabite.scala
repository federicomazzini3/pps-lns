package lns.scenes.game.enemy.parabite

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Animations.Parabite
import lns.core.anythingAssets.ParabiteAsset
import lns.scenes.game.anything.DynamicState
import lns.scenes.game.enemy.EnemyState

/**
 * Parabite enemy view elements builder
 */
trait Parabite extends ParabiteAsset {

  val bodySprite: Sprite[Material.Bitmap] = spriteAnimation("parabite_body")

  /**
   * Builds the body view
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   body view Sprite
   */
  def bodyView(model: ParabiteModel, viewModel: ParabiteViewModel): Sprite[Material.Bitmap] =
    bodyAnimation(model, viewModel)
      .withRef(Parabite.bodyWidth / 2, 0)
      .flipHorizontal(toFlip(model))
  // .moveTo(width / 2, 0)

  /**
   * An asset image may be represented only in right position instead of left + right. In this case we need to flip it
   * if the model is moving leftward
   * @param model
   *   the [[ParabiteModel]]
   * @return
   *   true if image need to be flipped to represent current dynamic state.
   */
  def toFlip(model: ParabiteModel): Boolean = model.getDynamicState() match {
    case DynamicState.MOVE_LEFT => true
    case _                      => false
  }

  /**
   * Calculates current animation frame for the hiding animation
   * @param timer
   *   current animation timer from X to 0
   * @return
   *   anim frame
   */
  def getFrame(timer: Double): Int =
    Math.floor((Parabite.hideTime - timer) / Parabite.hideFrameTime).toInt

  /**
   * Plays the animation cycle if the character is moving
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   the updated body Sprite
   */
  def bodyAnimation(model: ParabiteModel, viewModel: ParabiteViewModel): Sprite[Material.Bitmap] =
    model.status.head match {
      case (EnemyState.Attacking, _) => bodySprite.changeCycle(CycleLabel("walking")).play()
      case (EnemyState.Hiding, _) =>
        viewModel.animationTimer match {
          case 0 => bodySprite.changeCycle(CycleLabel("hiding")).jumpToLastFrame()
          case t =>
            bodySprite
              .changeCycle(CycleLabel("hiding"))
              .jumpToFrame(getFrame(t))
        }
      case (EnemyState.Idle, _) =>
        viewModel.animationTimer match {
          case 0 => bodySprite.changeCycle(CycleLabel("wakeup")).jumpToLastFrame()
          case t =>
            bodySprite
              .changeCycle(CycleLabel("wakeup"))
              .jumpToFrame(getFrame(t))
        }
      case _ => bodySprite.changeCycle(CycleLabel("idle")).jumpToFirstFrame()
    }

}
