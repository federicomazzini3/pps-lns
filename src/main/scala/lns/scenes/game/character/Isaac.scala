package lns.scenes.game.character

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Assets
import lns.core.Animations.*
import lns.scenes.game.anything.*

/**
 * Isaac Character view elements builder
 */
trait Isaac {
  val width: Int  = 28
  val height: Int = 33
  val scale: Int  = 2

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
      BindingKey("character_body_sprite"),
      0,
      0,
      1,
      AnimationKey("character_body"),
      Material.Bitmap(Assets.Character.character)
    )

  def headManualAnimation(model: CharacterModel, viewModel: CharacterViewModel): Rectangle =
    viewModel.fireAnimationTimer match {
      case x if x > 0 && x > model.fireRate / 2 => Character.headCrop(viewModel.fireState, false)
      case x if x > 0                           => Character.headCrop(viewModel.fireState, true)
      case _ =>
        model.isFiring() match {
          case true => Character.headCrop(model.getFireState(), false)
          case _    => Character.headCrop(model.getDynamicState(), true)
        }
    }

  def headView(model: CharacterModel, viewModel: CharacterViewModel): Graphic[Material.Bitmap] =
    Graphic(headManualAnimation(model, viewModel), 1, Material.Bitmap(Assets.Character.character))
      .withRef(0, 0)
      .moveTo(0, 0)

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

  /*
   val headSprite: Sprite[Material.Bitmap] =
     Sprite(
       BindingKey("character_head_sprite"),
       0,
       0,
       1,
       AnimationKey("character_head"),
       Material.Bitmap(Assets.Character.character)
     )

   def headAnimation(model: CharacterModel, viewModel: CharacterViewModel): Sprite[Material.Bitmap] =
     viewModel.fireAnimationTimer match {
       case x if x > 0 => headSprite.changeCycle(headAnimationFiringCycle(viewModel.fireState)).play()
       case _ =>
         model.isFiring() match {
           case true => headSprite.changeCycle(headAnimationFiringCycle(model.getFireState())).jumpToFirstFrame().play()
           case _    => headSprite.changeCycle(headAnimationMovingCycle(model.getDynamicState())).jumpToLastFrame()
         }
     }

   def headAnimationFiringCycle(state: FireState): CycleLabel = state match {
     case FireState.FIRE_UP    => CycleLabel("up_shot")
     case FireState.FIRE_RIGHT => CycleLabel("right_shot")
     case FireState.FIRE_LEFT  => CycleLabel("left_shot")
     case _                    => CycleLabel("down_shot")
   }

   def headAnimationMovingCycle(state: DynamicState): CycleLabel = state match {
     case DynamicState.MOVE_UP    => CycleLabel("up_shot")
     case DynamicState.MOVE_RIGHT => CycleLabel("right_shot")
     case DynamicState.MOVE_LEFT  => CycleLabel("left_shot")
     case _                       => CycleLabel("down_shot")
   }
   */
}
