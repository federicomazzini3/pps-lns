package lns.scenes.game.character

import indigo.*
import indigoextras.geometry.BoundingBox
import indigoextras.geometry.Vertex

trait Anything {
  val model: Shape
  val boundingBox: BoundingBox
  val life: Option[Int]

  val position: Vertex            = Vertex(boundingBox.horizontalCenter, boundingBox.bottom)
  def draw(): SceneUpdateFragment = SceneUpdateFragment(model)
  def update(gameTime: GameTime, inputState: InputState): Anything
}

object DynamicState extends Enumeration {
  type DynamicState = Value
  val IDLE, MOVE_LEFT, MOVE_RIGHT, MOVE_DOWN, MOVE_UP = Value
}
import DynamicState._

trait Dynamic extends Anything {
  val speed: Double = 1.0
  val state: DynamicState

  def isMoving(state: DynamicState): Boolean = state match {
    case IDLE => false
    case _    => true
  }
}

case class Character(
    boundingBox: BoundingBox,
    state: DynamicState,
    life: Option[Int]
) extends Anything
    with Dynamic {

  val model: Shape = Shape.Box(
    boundingBox.toRectangle,
    Fill.Color(RGBA.White),
    Stroke(4, RGBA.Blue)
  )

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
    Character(boundingBox.moveBy(inputForce.x, inputForce.y), IDLE, Some(10))
}

object Character {
  val initial: Character = Character(BoundingBox(Vertex(30, 30), Vertex(20, 20)), IDLE, Some(10))
}
