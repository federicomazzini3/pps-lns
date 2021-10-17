package lns.scenes.game.character

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Group, Shape }
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex
import lns.StartupData
import lns.core.Assets
import lns.scenes.game.anything.*

trait Isaac {
  import lns.scenes.game.anything.DynamicState._

  val width: Int  = 28
  val height: Int = 33
  val scale: Int  = 2

  import lns.core.Animations.*

  /* Head */
  def headCrop(model: CharacterModel): Rectangle = model.getState() match {
    case MOVE_LEFT  => Rectangle(250, 25, Character.headWidth, Character.headHeight)
    case MOVE_RIGHT => Rectangle(90, 25, Character.headWidth, Character.headHeight)
    case MOVE_UP    => Rectangle(170, 25, Character.headWidth, Character.headHeight)
    case _          => Rectangle(10, 25, Character.headWidth, Character.headHeight)
  }

  def headModel(model: CharacterModel): Graphic[Material.Bitmap] =
    Graphic(headCrop(model), 1, Material.Bitmap(Assets.Character.character))
      .withRef(0, 0)
      .moveTo(0, 0)

  /* Body */
  val bodySprite: Sprite[Material.Bitmap] =
    Sprite(
      BindingKey("character_body_sprite"),
      0,
      0,
      1,
      AnimationKey("character_body"),
      Material.Bitmap(Assets.Character.character)
    )

  def bodyFlip(model: CharacterModel): Boolean = model.getState() match {
    case MOVE_LEFT => true
    case _         => false
  }
  def bodyAnimationCycle(model: CharacterModel): CycleLabel = model.getState() match {
    case MOVE_LEFT | MOVE_RIGHT => CycleLabel("walking_left_right")
    case _                      => CycleLabel("walking_up_down")
  }
  def bodyAnimation(model: CharacterModel): Sprite[Material.Bitmap] = model.isMoving() match {
    case true => bodySprite.changeCycle(bodyAnimationCycle(model)).play()
    case _    => bodySprite.changeCycle(bodyAnimationCycle(model)).jumpToFirstFrame()
  }
  def bodyModel(model: CharacterModel): Sprite[Material.Bitmap] = bodyAnimation(model)
    .withRef(Character.bodyWidth / 2, 0)
    .flipHorizontal(bodyFlip(model))
    .moveTo(width / 2, 20)

  /*Shadow*/
  val shadowModel: Shape =
    Shape
      .Circle(
        center = Point(width / 2, height + width / 4),
        radius = width / 3,
        Fill.Color(RGBA(0, 0, 0, 0.4))
      )
      .scaleBy(1, 0.25)

  /*Bounding*/
  val boundingModel: Shape =
    Shape.Box(
      Rectangle(Point(0, 0), Size(width, height)),
      Fill.Color(RGBA(1, 1, 1, 0.5))
    )
}

case class CharacterView() extends AnythingView with Isaac {

  type Model     = CharacterModel
  type ViewModel = Unit
  type View      = Group

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    Group()
      /* .addChild(boundingModel) */
      .addChild(shadowModel)
      .addChild(bodyModel(model))
      .addChild(headModel(model))
      .withRef(width / 2, height / 2)
      .withScale(Vector2(scale, scale))

}
