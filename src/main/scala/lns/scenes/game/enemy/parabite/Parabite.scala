package lns.scenes.game.enemy.parabite

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Animations.Parabite
import lns.core.Assets
import lns.scenes.game.anything.DynamicState
import lns.scenes.game.enemy.EnemyState

/**
 * Isaac Character view elements builder
 */
trait Parabite {

  import Assets.Enemies.Parabite.*

  val boundingModel: Shape =
    Shape.Box(
      Rectangle(Point(0, 0), Size(width, height)),
      Fill.Color(RGBA(1, 1, 1, 0.5))
    )

  val bodySprite: Sprite[Material.Bitmap] =
    Sprite(
      BindingKey("parabite_body_sprite"),
      0,
      0,
      1,
      AnimationKey("parabite_body"),
      Material.Bitmap(name)
    )

  /**
   * An asset image may be represented only in right position instead of left + right. In this case we need to flip it
   * if the model is moving leftward
   *
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
   * Builds the body view Sprite
   *
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
   * Plays the animation cycle if the character is moving
   *
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   the updated body Sprite
   */
  def bodyAnimation(model: ParabiteModel, viewModel: ParabiteViewModel): Sprite[Material.Bitmap] = model.status match {
    case EnemyState.Attacking => bodySprite.changeCycle(CycleLabel("walking")).play()
    case EnemyState.Hiding =>
      viewModel.animationTimer match {
        case 0 => bodySprite.changeCycle(CycleLabel("hiding")).jumpToLastFrame()
        case _ => bodySprite.changeCycle(CycleLabel("hiding")).play()
      }
    case EnemyState.Idle =>
      viewModel.animationTimer match {
        case 0 => bodySprite.changeCycle(CycleLabel("wakeup")).jumpToLastFrame()
        case _ => bodySprite.changeCycle(CycleLabel("wakeup")).play()
      }
    case _ => bodySprite.changeCycle(CycleLabel("idle")).jumpToFirstFrame()
  }

}
