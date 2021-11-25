package lns.scenes.game.anything

import scala.language.implicitConversions
import indigo.*
import indigo.shared.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.scenes.game.GameContext
import lns.scenes.game.characters.CharacterModel
import lns.scenes.game.room.{ Boundary, RoomModel }
import lns.scenes.game.shots.{ ShotEvent, ShotModel, ShotView }
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.*
import lns.scenes.game.subsystems.*

import java.util.UUID

/**
 * represents an unique identification string for an Anything instance
 */
type AnythingId = UUID
object AnythingId {
  def generate: AnythingId = UUID.randomUUID()
}

given Conversion[Vector2, Vertex] with
  def apply(v: Vector2): Vertex = Vertex(v.x, v.y)

given Conversion[Set[Outcome[AnythingModel]], Outcome[Set[AnythingModel]]] with
  def apply(set: Set[Outcome[AnythingModel]]): Outcome[Set[AnythingModel]] =
    set.foldLeft(Outcome(Set[AnythingModel]().empty))((acc, el) => acc.merge(el)((set, el) => set + el))

given Conversion[Map[AnythingId, Outcome[AnythingModel]], Outcome[Map[AnythingId, AnythingModel]]] with
  def apply(set: Map[AnythingId, Outcome[AnythingModel]]): Outcome[Map[AnythingId, AnythingModel]] =
    set.foldLeft(Outcome(Map[AnythingId, AnythingModel]().empty))((acc, el) =>
      acc.merge(el._2)((set, el2) => set + (el._1 -> el2))
    )

/**
 * type alias of Double pimped with the elapsed method
 */
type Timer = Double
extension (timer: Timer)
  /**
   * @param time
   *   quantity of time to subtract to the timer
   * @return
   *   a new Timer with time subtracted
   */
  def elapsed(time: Double): Timer = timer match {
    case 0                 => 0
    case x if x - time > 0 => x - time
    case _                 => 0
  }

/**
 * Base model for every thing placed inside a room
 */
trait AnythingModel {
  type Model >: this.type <: AnythingModel

  /**
   * The unique identifier of the Anything instance.
   */
  val id: AnythingId

  /**
   * The Anything's View factory. The View needs to be designed for the specific Model
   */
  val view: () => AnythingView[Model, _]

  /**
   * Represents the position and the box size of the Anything, expressed in pixels
   */
  val boundingBox: BoundingBox

  /**
   * @return
   *   the current position Vector2
   */
  def getPosition(): Vector2 = Vector2(boundingBox.left, boundingBox.top)

  /**
   * Update request called during game loop on every frame
   * @param context
   *   indigo frame context data
   * @param gameContext
   *   current [[GameContext]] containing the current room in which the Anything is placed and the character model
   * @return
   *   the Outcome of the updated model
   */
  def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    Outcome(this)
}

/**
 * Base model for every object that can have stats. It is designed to be extended or mixed with other [[AnythingModel]]
 * traits.
 */
trait StatsModel {
  type Model >: this.type <: StatsModel

  val stats: Stats

  def withStats(stats: Stats): Model

  /**
   * Replace all models stats
   * @param context
   *   indigo frame context data
   * @param newStats
   *   the new [[Stats]] object to replace with current
   * @return
   *   the Outcome of the updated model
   */
  def changeStats(context: FrameContext[StartupData], newStats: Stats): Outcome[Model] = Outcome(withStats(newStats))

  /**
   * Replace single models stat
   * @param context
   *   indigo frame context data
   * @param property
   *   [[property]] to replace
   * @return
   *   the Outcome of the updated model
   */
  def changeStat(context: FrameContext[StartupData], property: StatProperty): Outcome[Model] =
    Outcome(withStats(stats + property))

  /**
   * Upldate single models stat
   * @param context
   *   indigo frame context data
   * @param property
   *   [[property]] to sum with current property
   * @return
   *   the Outcome of the updated model
   */
  def sumStat(context: FrameContext[StartupData], property: StatProperty): Outcome[Model] =
    Outcome(withStats(stats +++ property))

}

enum DynamicState:
  case IDLE, MOVE_LEFT, MOVE_RIGHT, MOVE_DOWN, MOVE_UP

import DynamicState.*

/**
 * Base model for every object that can move. It is designed to be extended or mixed with other [[AnythingModel]]
 * traits.
 */
trait DynamicModel extends AnythingModel with StatsModel {
  type Model >: this.type <: DynamicModel

  val speed: Vector2
  val collisionDetected: Boolean

  def withDynamic(boundingBox: BoundingBox, speed: Vector2, collisionDetected: Boolean): Model

  /**
   * @return
   *   Boolean: true if the model is moving otherwise false
   */
  def isMoving(): Boolean = getDynamicState() match {
    case IDLE => false
    case _    => true
  }

  /**
   * @return
   *   the [[DynamicState]] based on the current speed vector
   */
  def getDynamicState(): DynamicState = speed match {
    case Vector2(x, y) if x < 0 && Math.abs(x) >= Math.abs(y) => MOVE_LEFT
    case Vector2(x, y) if x > 0 && Math.abs(x) >= Math.abs(y) => MOVE_RIGHT
    case Vector2(_, y) if y < 0                               => MOVE_UP
    case Vector2(_, y) if y > 0                               => MOVE_DOWN
    case _                                                    => IDLE
  }

  /**
   * Computes the Anything speed vector in px/s unit. Represents the current Anything intention of movement
   * @param context
   *   indigo frame context data
   * @param gameContext
   *   current [[GameContext]] containing the current room in which the Anything is placed and the character model
   * @return
   *   the speed vector
   */
  def computeSpeed(context: FrameContext[StartupData])(gameContext: GameContext): Vector2

  /**
   * Calculates the speed vector for the current frame that represents how many pixels the Anything has to move in
   * current frame update
   * @param context
   *   indigo frame context data
   * @param speed
   *   speed Vector2
   * @return
   *   the speed vector for the current frame
   */
  protected def frameSpeed(context: FrameContext[StartupData], speed: Vector2): Vector2 =
    speed * context.gameTime.delta.toDouble

  /**
   * Limit the movement to do in the current frame update, default no limits. To be overriden when needed to specify a
   * custom limit rule
   * @param move
   *   current movement Vector2
   * @return
   *   current movement Vector2 limited by a rule, default no limit rule
   */
  protected def limitMove(move: Vector2): Vector2 = move

  /**
   * @param context
   *   indigo frame context data
   * @param room
   *   current room in which the Anything is placed
   * @param character
   *   character model as [[AnythingModel]]
   * @return
   *   a Tuple2 representing the computed speed and the moved boundingBox
   */
  def computeMove(context: FrameContext[StartupData])(gameContext: GameContext): (Vector2, BoundingBox) =
    val speed: Vector2 = computeSpeed(context)(gameContext)
    speed match {
      case Vector2.zero => (speed, boundingBox)
      case x            => (x, boundingBox.moveBy(limitMove(frameSpeed(context, x))))
    }

  def movedBy(previous: DynamicModel): Double = getPosition() - previous.getPosition() match {
    case v if v ~== Vector2.zero => 0.0
    case v                       => v.length
  }

  /**
   * Update request called during game loop on every frame. The Anything movement speed direction and module is first
   * calculated and then validated by the current room
   * @param context
   *   indigo frame context data
   * @param gameContext
   *   current [[GameContext]] containing the current room in which the Anything is placed and the character model
   * @return
   *   the Outcome of the updated model wich is moved by its speed vector data normalized on gameTime.delta
   */
  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    for {
      superObj <- super.update(context)(gameContext)
      (newSpeed, newPosition) = computeMove(context)(gameContext)
      boundLocation           = gameContext.room.boundPosition(newPosition)
      newObj = superObj
        .withDynamic(boundLocation, newSpeed, boundLocation.position != newPosition.position)
        .asInstanceOf[Model]
    } yield newObj

}

/**
 * Base model for every object that is alive and can be damaged. It is designed to be extended or mixed with other
 * [[AnythingModel]] traits.
 */
trait AliveModel extends AnythingModel with StatsModel {
  type Model >: this.type <: AliveModel

  val life: Double
  val invincibilityTimer: Timer

  def withAlive(life: Double, invincibilityTimer: Timer): Model

  /**
   * Hit the object causing some damage to its life and starting a countdown timer during which it can't be hitted again
   * @param context
   *   indigo frame context data
   * @param damage
   *   the value of life to be subtracted
   * @return
   *   the Outcome of the updated model
   */
  def hit(context: FrameContext[StartupData], damage: Double): Outcome[Model] = invincibilityTimer match {
    case 0 if life - damage > 0 => Outcome(withAlive(life - damage, Invincibility @@ stats)).addGlobalEvents(Hit(this))
    case 0                      => Outcome(withAlive(0, 0)).addGlobalEvents(Dead(this))
    case _                      => Outcome(this)
  }

  /**
   * Update request called during game loop on every frame. After a hit the Anything life status is protected for the
   * invincibility time during which the thing can't be damaged. Each frame update we have to check and update the timer
   * ultil its expiration
   * @param context
   *   indigo frame context data
   * @param gameContext
   *   current [[GameContext]] containing the current room in which the Anything is placed and the character model
   * @return
   *   the Outcome of the updated model
   */
  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    for {
      superObj <- super.update(context)(gameContext)
      newObj =
        invincibilityTimer match {
          case 0 => superObj
          case x => superObj.withAlive(life, x.elapsed(context.gameTime.delta.toDouble)).asInstanceOf[Model]
        }
    } yield newObj

}

/**
 * Base model for every object that make damage on contact to other model that extends [[AliveModel]] trait. It is
 * designed to be extended or mixed with other [[AnythingModel]] traits.
 */
trait DamageModel extends AnythingModel with StatsModel {
  type Model >: this.type <: DamageModel

}

enum FireState:
  case NO_FIRE, FIRE_LEFT, FIRE_RIGHT, FIRE_DOWN, FIRE_UP

import FireState.*

/**
 * Base model for every object that can fire. It is designed to be extended or mixed with other [[AnythingModel]]
 * traits.
 */
trait FireModel extends AnythingModel with StatsModel {
  type Model >: this.type <: FireModel

  val shot: Option[Vector2]
  val fireRateTimer: Double
  val shotOffset: Int
  val shotView: () => ShotView[_]

  def withFire(fireRateTimer: Timer, shot: Option[Vector2]): Model

  /**
   * @return
   *   Boolean: true if the model is firing otherwise false
   */
  def isFiring(): Boolean = getFireState() match {
    case NO_FIRE => false
    case _       => true
  }

  /**
   * @return
   *   the [[FireState]] based on the current optional shot direction
   */
  def getFireState(): FireState = shot match {
    case Some(Vector2(x, y)) if x < 0 && Math.abs(x) >= Math.abs(y) => FIRE_LEFT
    case Some(Vector2(x, y)) if x > 0 && Math.abs(x) >= Math.abs(y) => FIRE_RIGHT
    case Some(Vector2(_, y)) if y < 0                               => FIRE_UP
    case Some(Vector2(_, y)) if y > 0                               => FIRE_DOWN
    case _                                                          => NO_FIRE
  }

  /**
   * @param context
   *   indigo frame context data * @param gameContext
   * @param gameContext
   *   current [[GameContext]] containing the current room in which the Anything is placed and the character model
   * @return
   *   Optional direction vector
   */
  def computeFire(context: FrameContext[StartupData])(gameContext: GameContext): Option[Vector2]

  /**
   * Create a new ShotEvent capable of being captured by the game model during game loop on every frame
   * @param Vector2
   *   direction vector
   * @return
   *   ShotEvent
   */
  def createEvent(direction: Vector2): ShotEvent =
    ShotEvent(
      ShotModel(
        id,
        Vector2(boundingBox.horizontalCenter, boundingBox.top + shotOffset),
        direction,
        Stats.createShot(stats)
      )(shotView)
    )

  /**
   * Update request called during game loop on every frame. Check if there is a firing computation, if there is no timer
   * that limits the firing rate, a global event is created and intercepted by the [[GameView]] Otherwise, if only the
   * timer is present, it is decreased to 0
   * @param context
   *   indigo frame context data
   * @param gameContext
   *   current [[GameContext]] containing the current room in which the Anything is placed and the character model
   * @return
   *   the Outcome of the updated model
   */
  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    val newFireRateTimer = fireRateTimer.elapsed(context.gameTime.delta.toDouble)
    val newShot          = computeFire(context)(gameContext)

    val retObj = for {
      superObj <- super.update(context)(gameContext)
      newObj = (newFireRateTimer, newShot) match {
        case (0, Some(_)) => superObj.withFire(FireRate @@ stats, newShot).asInstanceOf[Model]
        case _            => superObj.withFire(newFireRateTimer, newShot).asInstanceOf[Model]
      }
    } yield newObj

    (newFireRateTimer, newShot) match {
      case (0, Some(direction)) => retObj.addGlobalEvents(createEvent(direction))
      case _                    => retObj
    }
}

/**
 * Base model for every solid object. It is designed to be extended or mixed with other [[AnythingModel]] traits.
 */
trait SolidModel extends AnythingModel {
  type Model >: this.type <: SolidModel

  val crossable: Boolean
  val shotAreaOffset: Int

  def withSolid(crossable: Boolean): Model

  val shotArea: BoundingBox = boundingBox
    .resize(Vector2(boundingBox.size.x, boundingBox.size.y - shotAreaOffset))
    .moveBy(0, shotAreaOffset)
}
