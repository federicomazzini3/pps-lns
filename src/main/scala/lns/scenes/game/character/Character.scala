package lns.scenes.game.character

import indigo.*
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex
import lns.StartupData
import lns.core.Assets
import lns.core.Animations

trait Anything {
  val model: Group

  val boundingBox: BoundingBox
  val life: Option[Int]

  val position: Vertex            = Vertex(boundingBox.horizontalCenter, boundingBox.bottom)
  def draw(): SceneUpdateFragment = SceneUpdateFragment(model)
  def update(gameTime: GameTime, inputState: InputState): Anything

}

enum DynamicState {
  case IDLE, MOVE_LEFT, MOVE_RIGHT, MOVE_DOWN, MOVE_UP
}
import DynamicState._

trait Dynamic extends Anything {
  val speed: Double = 3.0
  val state: DynamicState

  def isMoving: Boolean = state match {
    case IDLE => false
    case _    => true
  }

  def getNewState(v: Vector2): DynamicState = v match {
    case Vector2(x, _) if x < 0 => MOVE_LEFT
    case Vector2(x, _) if x > 0 => MOVE_RIGHT
    case Vector2(_, y) if y < 0 => MOVE_UP
    case Vector2(_, y) if y > 0 => MOVE_DOWN
    case _                      => IDLE
  }
}

case class Character(
    boundingBox: BoundingBox,
    state: DynamicState,
    life: Option[Int]
) extends Anything
    with Dynamic {

  val width: Int  = 28
  val height: Int = 33
  val scale: Int  = 3

  /* Head */
  val headWidth: Int  = 28
  val headHeight: Int = 25
  val headCrop: Rectangle = this.state match {
    case MOVE_LEFT  => Rectangle(250, 25, headWidth, headHeight)
    case MOVE_RIGHT => Rectangle(90, 25, headWidth, headHeight)
    case MOVE_UP    => Rectangle(170, 25, headWidth, headHeight)
    case _          => Rectangle(10, 25, headWidth, headHeight)
  }

  val headModel: Graphic[Material.Bitmap] =
    Graphic(headCrop, 1, Material.Bitmap(Assets.Character.character))
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

  val bodyFlip: Boolean = state match {
    case MOVE_LEFT => true
    case _         => false
  }
  val bodyAnimationCycle: CycleLabel = state match {
    case MOVE_LEFT | MOVE_RIGHT => CycleLabel("walking_left_right")
    case _                      => CycleLabel("walking_up_down")
  }
  val bodyAnimation: Sprite[Material.Bitmap] = isMoving match {
    case true => bodySprite.changeCycle(bodyAnimationCycle).play()
    case _    => bodySprite.changeCycle(bodyAnimationCycle).jumpToFirstFrame()
  }
  val bodyModel: Sprite[Material.Bitmap] = bodyAnimation
    .withRef(Animations.Character.bodyWidth / 2, 0)
    .flipHorizontal(bodyFlip)
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

  /* Model */
  val model: Group =
    Group()
      /* .addChild(boundingModel) */
      .addChild(shadowModel)
      .addChild(bodyModel)
      .addChild(headModel)
      .withRef(width / 2, height / 2)
      .withScale(Vector2(scale, scale))
      .moveTo(boundingBox.x.toInt, boundingBox.y.toInt)

  val inputMappings: InputMapping[Vector2] =
    InputMapping(
      Combo.withKeyInputs(Key.LEFT_ARROW, Key.UP_ARROW)    -> Vector2(-speed, -speed),
      Combo.withKeyInputs(Key.LEFT_ARROW, Key.DOWN_ARROW)  -> Vector2(-speed, speed),
      Combo.withKeyInputs(Key.LEFT_ARROW)                  -> Vector2(-speed, 0.0d),
      Combo.withKeyInputs(Key.RIGHT_ARROW, Key.UP_ARROW)   -> Vector2(speed, -speed),
      Combo.withKeyInputs(Key.RIGHT_ARROW, Key.DOWN_ARROW) -> Vector2(speed, speed),
      Combo.withKeyInputs(Key.RIGHT_ARROW)                 -> Vector2(speed, 0.0d),
      Combo.withKeyInputs(Key.UP_ARROW)                    -> Vector2(0.0d, -speed),
      Combo.withKeyInputs(Key.DOWN_ARROW)                  -> Vector2(0.0d, speed)
    )

  override def update(gameTime: GameTime, inputState: InputState): Character =
    val inputForce = inputState.mapInputs(inputMappings, Vector2.zero)
    val newState   = getNewState(inputForce)
    Character(boundingBox.moveBy(inputForce.x, inputForce.y), newState, Some(10))
}

object Character {
  def initial(startupData: StartupData): Character = Character(
    BoundingBox(
      Vertex(startupData.screenDimensions.horizontalCenter, startupData.screenDimensions.verticalCenter),
      Vertex(28, 33)
    ),
    IDLE,
    Some(10)
  )
}
