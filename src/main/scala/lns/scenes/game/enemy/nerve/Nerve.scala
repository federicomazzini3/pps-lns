package lns.scenes.game.enemy.nerve

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Animations.Nerve
import lns.core.Assets

/**
 * Nerve Enemy view elements builder
 */
trait Nerve {

  import Assets.Enemies.Nerve.*

  val boundingModel: Shape =
    Shape.Box(
      Rectangle(Point(0, 0), Size(width, height)),
      Fill.Color(RGBA(1, 1, 1, 0.5))
    )

  val bodySprite: Sprite[Material.Bitmap] =
    Sprite(
      BindingKey("nerve_body_sprite"),
      0,
      0,
      1,
      AnimationKey("nerve_body"),
      Material.Bitmap(name)
    )

  /**
   * Builds the body view Sprite
   *
   * @param model
   *   the [[NerveModel]]
   * @return
   *   body view Sprite
   */
  def bodyView(model: NerveModel): Sprite[Material.Bitmap] = bodyAnimation(model)
    .withRef(Nerve.bodyWidth / 2, 0)
    .moveTo(width / 2, 20)

  /**
   * Plays the animation cycle if the character is moving
   *
   * @param model
   *   the [[BoneyModel]]
   * @return
   *   the updated body Sprite
   */
  def bodyAnimation(model: NerveModel): Sprite[Material.Bitmap] = bodySprite.changeCycle(CycleLabel("idle")).play()

}
