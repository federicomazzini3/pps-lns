package lns.scenes.game.character

import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.{ AliveModel, DynamicModel }

/**
 * Character model that is alive and can move computing its speed by user input
 * @param boundingBox
 *   [[DynamicModel]] boundingBox
 * @param speed
 *   [[DynamicModel]] speed
 * @param life
 *   [[AliveModel]] life
 * @param invincibilityTimer
 *   [[AliveModel]] invincibilityTimer, default 0
 */
case class CharacterModel(boundingBox: BoundingBox, speed: Vector2, life: Int, invincibilityTimer: Double = 0)
    extends AliveModel
    with DynamicModel {

  type Model = CharacterModel

  val maxSpeed              = 120
  val invincibility: Double = 1.5

  val inputMappings: InputMapping[Vector2] =
    InputMapping(
      Combo.withKeyInputs(Key.LEFT_ARROW, Key.UP_ARROW)    -> Vector2(-maxSpeed, -maxSpeed),
      Combo.withKeyInputs(Key.LEFT_ARROW, Key.DOWN_ARROW)  -> Vector2(-maxSpeed, maxSpeed),
      Combo.withKeyInputs(Key.LEFT_ARROW)                  -> Vector2(-maxSpeed, 0.0d),
      Combo.withKeyInputs(Key.RIGHT_ARROW, Key.UP_ARROW)   -> Vector2(maxSpeed, -maxSpeed),
      Combo.withKeyInputs(Key.RIGHT_ARROW, Key.DOWN_ARROW) -> Vector2(maxSpeed, maxSpeed),
      Combo.withKeyInputs(Key.RIGHT_ARROW)                 -> Vector2(maxSpeed, 0.0d),
      Combo.withKeyInputs(Key.UP_ARROW)                    -> Vector2(0.0d, -maxSpeed),
      Combo.withKeyInputs(Key.DOWN_ARROW)                  -> Vector2(0.0d, maxSpeed)
    )

  def withAlive(life: Int, invincibilityTimer: Double): Model      = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro

  def computeSpeed(context: FrameContext[StartupData]): Vector2 =
    context.inputState.mapInputs(inputMappings, Vector2.zero)
}

/**
 * Factory of [[CharacterModel]]
 */
object CharacterModel {
  def initial(startupData: StartupData): CharacterModel = CharacterModel(
    BoundingBox(
      Vertex(startupData.screenDimensions.horizontalCenter, startupData.screenDimensions.verticalCenter),
      Vertex(28, 33)
    ),
    Vector2(0, 0),
    10
  )
}
