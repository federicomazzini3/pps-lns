package lns.scenes.game.anything

import indigo.*
import indigo.shared.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.scenes.game.room.{ Boundary, RoomModel }
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
  def getPosition(): Vector2 = Vector2(boundingBox.horizontalCenter, boundingBox.top)

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

  def isMoving(): Boolean = getState() match {
    case IDLE => false
    case _    => true
  }

  /**
   * @return
   *   the [[DynamicState]] based on the current speed vector
   */
  def getState(): DynamicState = speed match {
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
      newLocation = Boundary.positionBounded(room.floor, boundingBox.moveBy(newSpeed))
      newObj      = superObj.edit(boundingBox.moveTo(newLocation), newSpeed).asInstanceOf[Model]
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
