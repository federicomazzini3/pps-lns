package lns.scenes.game.character

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Assets

/**
 * Isaac Character view elements builder
 */
trait Isaac {
  import lns.scenes.game.anything.DynamicState._

  val width: Int  = 28
  val height: Int = 33
  val scale: Int  = 2

  import lns.core.Animations.*

  /**
   * @param model
   *   the [[CharacterModel]]
   * @return
   *   head image crop coordinates based on current dynamic state
   */
  def headCrop(model: CharacterModel): Rectangle = model.getState() match {
    case MOVE_LEFT  => Rectangle(250, 25, Character.headWidth, Character.headHeight)
    case MOVE_RIGHT => Rectangle(90, 25, Character.headWidth, Character.headHeight)
    case MOVE_UP    => Rectangle(170, 25, Character.headWidth, Character.headHeight)
    case _          => Rectangle(10, 25, Character.headWidth, Character.headHeight)
  }

  /**
   * Builds the head view Graphic
   * @param model
   *   the [[CharacterModel]]
   * @return
   *   head view Graphic
   */
  def headView(model: CharacterModel): Graphic[Material.Bitmap] =
    Graphic(headCrop(model), 1, Material.Bitmap(Assets.Character.character))
      .withRef(0, 0)
      .moveTo(0, 0)

  val bodySprite: Sprite[Material.Bitmap] =
    Sprite(
      BindingKey("character_body_sprite"),
      0,
      0,
      1,
      AnimationKey("character_body"),
      Material.Bitmap(Assets.Character.character)
    )

  /**
   * @param model
   *   the [[CharacterModel]]
   * @return
   *   true if body image need to be flipped to represent current dynamic state
   */
  def bodyFlip(model: CharacterModel): Boolean = model.getState() match {
    case MOVE_LEFT => true
    case _         => false
  }

  /**
   * @param model
   *   the [[CharacterModel]]
   * @return
   *   the animation CycleLabel to be played based on current dynamic state
   */
  def bodyAnimationCycle(model: CharacterModel): CycleLabel = model.getState() match {
    case MOVE_LEFT | MOVE_RIGHT => CycleLabel("walking_left_right")
    case _                      => CycleLabel("walking_up_down")
  }

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
   * Builds the body view Sprite
   * @param model
   *   the [[CharacterModel]]
   * @return
   *   body view Sprite
   */
  def bodyView(model: CharacterModel): Sprite[Material.Bitmap] = bodyAnimation(model)
    .withRef(Character.bodyWidth / 2, 0)
    .flipHorizontal(bodyFlip(model))
    .moveTo(width / 2, 20)

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
}
