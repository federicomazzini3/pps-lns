package lns.scenes.game.characters

import scala.language.implicitConversions
import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ *, given }
import lns.scenes.game.shots.*
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

/**
 * Character model that is alive, it's dynamic by computing its speed and new position by user input, can fire computing
 * shot by user input and have stats
 * @param id
 *   [[AnythingId]] The unique identifier of the Anything instance.
 * @param view
 *   [[AnythingView]] The Anything's View factory function.
 * @param boundingBox
 *   [[AnythingModel]] boundingBox
 * @param shotAreaOffset
 *   shotAreaOffset
 * @param stats
 *   Initial [[Stats]]
 * @param crossable
 *   crossable, default false
 * @param life
 *   [[AliveModel]] life, default 0
 * @param invincibilityTimer
 *   [[AliveModel]] invincibilityTimer, default 0
 * @param speed
 *   [[DynamicModel]] speed, default Vector2(0, 0)
 * @param fireRateTimer
 *   [[FireModel]] fireRateTimer, default 0
 * @param shot
 *   [[FireModel]] shot, default None
 */
case class CharacterModel(
    id: AnythingId,
    view: () => CharacterView[_],
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    crossable: Boolean = false,
    life: Double = 0,
    invincibilityTimer: Double = 0,
    speed: Vector2 = Vector2(0, 0),
    fireRateTimer: Double = 0,
    shot: Option[Vector2] = None
) extends AliveModel
    with DynamicModel
    with FireModel
    with DamageModel
    with SolidModel {

  type Model = CharacterModel

  val shotView        = () => new SingleShotView() with ShotBlue
  val shotOffset: Int = -40

  // TODO: Builder pattern -> usare Require qui oppure sui Trait
  //  require ( life > 0 , " Incorrect life ")

  val moveInputMappings: InputMapping[Vector2] =
    val maxSpeed = MaxSpeed @@ stats
    InputMapping(
      Combo.withKeyInputs(Key.KEY_A, Key.KEY_W) -> Vector2(-maxSpeed, -maxSpeed),
      Combo.withKeyInputs(Key.KEY_A, Key.KEY_S) -> Vector2(-maxSpeed, maxSpeed),
      Combo.withKeyInputs(Key.KEY_A)            -> Vector2(-maxSpeed, 0.0d),
      Combo.withKeyInputs(Key.KEY_D, Key.KEY_W) -> Vector2(maxSpeed, -maxSpeed),
      Combo.withKeyInputs(Key.KEY_D, Key.KEY_S) -> Vector2(maxSpeed, maxSpeed),
      Combo.withKeyInputs(Key.KEY_D)            -> Vector2(maxSpeed, 0.0d),
      Combo.withKeyInputs(Key.KEY_W)            -> Vector2(0.0d, -maxSpeed),
      Combo.withKeyInputs(Key.KEY_S)            -> Vector2(0.0d, maxSpeed)
    )

  def withAlive(life: Double, invincibilityTimer: Double): Model    = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model  = copyMacro
  def withFire(fireRateTimer: Double, shot: Option[Vector2]): Model = copyMacro
  def withSolid(crossable: Boolean): Model                          = copyMacro
  def withStats(stats: Stats): Model                                = copyMacro

  def computeSpeed(context: FrameContext[StartupData])(gameContext: GameContext): Vector2 =
    context.inputState.mapInputs(moveInputMappings, Vector2.zero)

  val fireInputMappings: InputMapping[Vector2] =
    InputMapping(
      Combo.withKeyInputs(Key.UP_ARROW)    -> Vector2(0, -1),
      Combo.withKeyInputs(Key.RIGHT_ARROW) -> Vector2(1, 0),
      Combo.withKeyInputs(Key.DOWN_ARROW)  -> Vector2(0, 1),
      Combo.withKeyInputs(Key.LEFT_ARROW)  -> Vector2(-1, 0)
    )

  def computeFire(context: FrameContext[StartupData])(gameContext: GameContext): Option[Vector2] =
    context.inputState.mapInputsOption(fireInputMappings)
}

/**
 * Factory of [[CharacterModel]]
 */
object CharacterModel {

  def initial: CharacterModel = CharacterModel(
    id = AnythingId.generate,
    view = () => CharacterView,
    boundingBox = CharacterView.boundingBox(Vertex(Assets.Rooms.floorSize / 2, Assets.Rooms.floorSize / 2)),
    shotAreaOffset = CharacterView.shotAreaOffset,
    stats = Stats.Isaac,
    life = MaxLife @@ Stats.Isaac
  )
}
