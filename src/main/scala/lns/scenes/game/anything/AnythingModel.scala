package lns.scenes.game.anything

import indigo.*
import indigo.shared.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.scenes.game.room.{ Boundary, RoomModel }
import lns.scenes.game.shot.ShotEvent
import lns.scenes.game.stats.*

import scala.language.implicitConversions

given Conversion[Vector2, Vertex] with
  def apply(v: Vector2): Vertex = Vertex(v.x, v.y)

/**
 * Base model for every thing placed inside a room
 */
trait AnythingModel {
  type Model >: this.type <: AnythingModel

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
   * @param room
   *   current room in which the Anything is placed
   * @return
   *   the Outcome of the updated model
   */
  def update(context: FrameContext[StartupData])(room: RoomModel): Outcome[Model] = Outcome(this)
}

enum DynamicState {
  case IDLE, MOVE_LEFT, MOVE_RIGHT, MOVE_DOWN, MOVE_UP
}
import DynamicState.*

/**
 * Base model for every object that can move. It is designed to be extended or mixed with other [[AnythingModel]]
 * traits.
 */
trait DynamicModel extends AnythingModel {
  type Model >: this.type <: DynamicModel

  val speed: Vector2

  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model

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
    case Vector2(x, _) if x < 0 => MOVE_LEFT
    case Vector2(x, _) if x > 0 => MOVE_RIGHT
    case Vector2(_, y) if y < 0 => MOVE_UP
    case Vector2(_, y) if y > 0 => MOVE_DOWN
    case _                      => IDLE
  }

  /**
   * @param context
   *   indigo frame context data
   * @return
   *   the speed vector
   */
  def computeSpeed(context: FrameContext[StartupData]): Vector2

  /**
   * Update request called during game loop on every frame. The Anything movement speed direction and module is first
   * calculated and then validated by the current room
   * @param context
   *   indigo frame context data
   * @param room
   *   current room in which the Anything is placed
   * @return
   *   the Outcome of the updated model wich is moved by its speed vector data normalized on gameTime.delta
   */
  override def update(context: FrameContext[StartupData])(room: RoomModel): Outcome[Model] =
    for {
      superObj <- super.update(context)(room)
      newSpeed    = computeSpeed(context) * context.gameTime.delta.toDouble
      newLocation = room.boundPosition(boundingBox.moveBy(newSpeed))
      newObj      = superObj.withDynamic(boundingBox.moveTo(newLocation), newSpeed).asInstanceOf[Model]
    } yield newObj

}

/**
 * Base model for every object that is alive and can be damaged. It is designed to be extended or mixed with other
 * [[AnythingModel]] traits.
 */
trait AliveModel extends AnythingModel {
  type Model >: this.type <: AliveModel

  val life: Int
  val invincibilityTimer: Double
  val invincibility: Double

  def withAlive(life: Int, invincibilityTimer: Double): Model

  /**
   * Hit the object causing some damage to its life and starting a countdown timer during which it can't be hitted again
   * @param context
   *   indigo frame context data
   * @param damage
   *   the value of life to be subtracted
   * @return
   *   the Outcome of the updated model
   */
  def hit(context: FrameContext[StartupData], damage: Int): Outcome[Model] = invincibilityTimer match {
    case 0 if life - damage > 0 => Outcome(withAlive(life - damage, invincibility))
    case 0                      => Outcome(withAlive(0, 0))
    case _                      => Outcome(this)
  }

  /**
   * Update request called during game loop on every frame. After a hit the Anything life status is protected for the
   * invincibility time during which the thing can't be damaged. Each frame update we have to check and update the timer
   * ultil its expiration
   * @param context
   *   indigo frame context data
   * @param room
   *   current room in which the Anything is placed
   * @return
   *   the Outcome of the updated model
   */
  override def update(context: FrameContext[StartupData])(room: RoomModel): Outcome[Model] =
    for {
      superObj <- super.update(context)(room)
      newObj = invincibilityTimer match {
        case 0 => superObj
        case _ if invincibilityTimer - context.gameTime.delta.toDouble > 0 =>
          superObj.withAlive(life, invincibilityTimer - context.gameTime.delta.toDouble).asInstanceOf[Model]
        case _ => superObj.withAlive(life, 0).asInstanceOf[Model]
      }
    } yield newObj

}

/**
 * Base model for every object that make damage on contact to other model that extends [[AliveModel]] trait. It is
 * designed to be extended or mixed with other [[AnythingModel]] traits.
 */
trait DamageModel extends AnythingModel {
  type Model >: this.type <: DamageModel

  val damage: Double
}

enum FireState {
  case NO_FIRE, FIRE_LEFT, FIRE_RIGHT, FIRE_DOWN, FIRE_UP
}
import FireState.*

/**
 * Base model for every object that can fire. It is designed to be extended or mixed with other [[AnythingModel]]
 * traits.
 */
trait FireModel extends AnythingModel {
  type Model >: this.type <: FireModel

  val shot: Option[Vector2]

  val fireDamage: Double
  val fireRange: Int
  val fireRate: Double
  val fireRateTimer: Double

  def withFire(fireRateTimer: Double, shot: Option[Vector2]): Model

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
    case Some(Vector2(x, _)) if x < 0 => FIRE_LEFT
    case Some(Vector2(x, _)) if x > 0 => FIRE_RIGHT
    case Some(Vector2(_, y)) if y < 0 => FIRE_UP
    case Some(Vector2(_, y)) if y > 0 => FIRE_DOWN
    case _                            => NO_FIRE
  }

  /**
   * @param context
   *   indigo frame context data
   * @return
   *   Optional direction vector
   */
  def computeFire(context: FrameContext[StartupData]): Option[Vector2]

  /**
   * Create a new ShotEvent capable of being captured by the game model during game loop on every frame
   * @param Vector2
   *   direction vector
   * @return
   *   ShotEvent
   */
  def createEvent(direction: Vector2): ShotEvent =
    ShotEvent(Vertex(boundingBox.horizontalCenter, boundingBox.verticalCenter), direction)

  /**
   * Update request called during game loop on every frame. Check if there is a firing computation, if there is no timer
   * that limits the firing rate, a global event is created and intercepted by the [[GameView]] Otherwise, if only the
   * timer is present, it is decreased to 0
   * @param context
   *   indigo frame context data
   * @param room
   *   current room in which the Anything is placed
   * @return
   *   the Outcome of the updated model
   */
  override def update(context: FrameContext[StartupData])(room: RoomModel): Outcome[Model] = {
    val shot = if (fireRateTimer == 0) computeFire(context) else None

    val retObj = for {
      superObj <- super.update(context)(room)
      newObj = fireRateTimer match {
        case 0 =>
          shot match {
            case Some(direction) => superObj.withFire(fireRate, Some(direction)).asInstanceOf[Model]
            case _               => superObj
          }
        case _ if fireRateTimer - context.gameTime.delta.toDouble > 0 =>
          superObj.withFire(fireRateTimer - context.gameTime.delta.toDouble, None).asInstanceOf[Model]
        case _ => superObj.withFire(0, None).asInstanceOf[Model]
      }
    } yield newObj

    shot match {
      case Some(direction) => retObj.addGlobalEvents(createEvent(direction))
      case _               => retObj
    }
  }
}

/**
 * Base model for every object that can have stats. It is designed to be extended or mixed with other [[AnythingModel]]
 * traits.
 */
trait StatsModel extends AnythingModel {
  type Model >: this.type <: StatsModel

  val stats: Stats

  def withStats(stats: Stats): Model
  def withStat[A <: Double](what: String)(value: A): Model

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
   * @param what
   *   the string that refers to a property of the [[Stats]] case class
   * @param value
   *   the value of the property to replace with current
   * @return
   *   the Outcome of the updated model
   */
  def changeStat[A <: Double](context: FrameContext[StartupData], what: String, value: A): Outcome[Model] = Outcome(
    withStat(what)(value)
  )
}
