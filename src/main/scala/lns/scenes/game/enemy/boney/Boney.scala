package lns.scenes.game.enemy.boney

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Animations.Boney
import lns.core.Assets
import lns.scenes.game.anything.DynamicState

/**
 * Isaac Character view elements builder
 */
trait Boney {

  import Assets.Enemies.Boney.*

  val shadowModel: Shape =
    Shape
      .Circle(
        center = Point(width / 2, height + width / 4),
        radius = width / 3,
        Fill.Color(RGBA(0, 0, 0, 0.4))
      )
      .scaleBy(1, 0.25)

  val boundingModel: Shape =
    Shape.Box(
      Rectangle(Point(0, 0), Size(width, height)),
      Fill.Color(RGBA(1, 1, 1, 0.5))
    )

  val bodySprite: Sprite[Material.Bitmap] =
    Sprite(
      BindingKey("boney_body_sprite"),
      0,
      0,
      1,
      AnimationKey("boney_body"),
      Material.Bitmap(name)
    )

  /*
  def headManualAnimation(model: EnemyModel, viewModel: EnemyViewModel): Rectangle =
    Boney.headCrop(model.getDynamicState())
   */

  /**
   * An asset image may be represented only in right position instead of left + right. In this case we need to flip it
   * if the model is moving leftward
   *
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   true if image need to be flipped to represent current dynamic state.
   */
  def toFlip(model: BoneyModel): Boolean = model.getDynamicState() match {
    case DynamicState.MOVE_LEFT => true
    case _                      => false
  }

  def headView(model: BoneyModel): Graphic[Material.Bitmap] =
    Graphic(Boney.headCrop(model.getDynamicState()), 1, Material.Bitmap(name))
      .withRef(0, 0)
      .flipHorizontal(toFlip(model))
      .moveTo(0, 0)

  /**
   * Builds the body view Sprite
   *
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   body view Sprite
   */
  def bodyView(model: BoneyModel): Sprite[Material.Bitmap] = bodyAnimation(model)
    .withRef(Boney.bodyWidth / 2, 0)
    .flipHorizontal(toFlip(model))
    .moveTo(width / 2, 20)

  /**
   * Plays the animation cycle if the character is moving
   *
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
