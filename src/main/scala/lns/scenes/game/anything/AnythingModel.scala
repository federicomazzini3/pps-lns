package lns.scenes.game.anything

import indigo.*
import indigo.shared.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.scenes.game.room.RoomModel

extension (b: BoundingBox) def moveBy(vector: Vector2) = b.moveBy(vector.x, vector.y)

/*Anything*/
trait AnythingModel {
  type Model >: this.type <: AnythingModel

  val boundingBox: BoundingBox

  def getPosition(): Vector2 = Vector2(boundingBox.horizontalCenter, boundingBox.top)

  def update(context: FrameContext[StartupData])(room: RoomModel): Outcome[Model] = Outcome(this)
}

/*Dynamic*/
enum DynamicState {
  case IDLE, MOVE_LEFT, MOVE_RIGHT, MOVE_DOWN, MOVE_UP
}
import DynamicState.*

trait DynamicModel extends AnythingModel {
  type Model >: this.type <: DynamicModel

  val speed: Vector2

  def isMoving(): Boolean = getState() match {
    case IDLE => false
    case _    => true
  }

  def getState(): DynamicState = speed match {
    case Vector2(x, _) if x < 0 => MOVE_LEFT
    case Vector2(x, _) if x > 0 => MOVE_RIGHT
    case Vector2(_, y) if y < 0 => MOVE_UP
    case Vector2(_, y) if y > 0 => MOVE_DOWN
    case _                      => IDLE
  }

  def computeSpeed(context: FrameContext[StartupData]): Vector2
  def edit(boundingBox: BoundingBox, speed: Vector2): Model

  override def update(context: FrameContext[StartupData])(room: RoomModel): Outcome[Model] =
    for {
      superObj <- super.update(context)(room)
      newSpeed    = computeSpeed(context)
      newLocation = boundingBox.moveBy(newSpeed)
      newObj =
        if (room.allowMoving(newLocation.position))
          superObj.edit(boundingBox.moveBy(newSpeed), newSpeed).asInstanceOf[Model]
        else superObj
    } yield newObj

  /*
    val superObj: Outcome[Model] = super.update(context);
    val superModel: Model        = superObj.unsafeGet
    val newSpeed                 = computeSpeed(context);
    val up: Model                = superModel.edit(boundingBox.moveBy(newSpeed), newSpeed).asInstanceOf[Model]

    Outcome(up)
   */

  /*
    superObj.map[Model] { (newObj: Model) =>
      val newSpeed  = computeSpeed(context);
      val up: Model = newObj.edit(boundingBox.moveBy(newSpeed), newSpeed);
    }
   */

}

trait AliveModel extends AnythingModel {
  type Model >: this.type <: AliveModel

  val life: Int
  val invincibilityTimer: Double
  val invincibility: Double

  def edit(life: Int, invincibilityTimer: Double): Model

  def hit(context: FrameContext[StartupData], danno: Int): Outcome[Model] = invincibilityTimer match {
    case 0 if life - danno > 0 => Outcome(edit(life - danno, invincibility))
    case 0                     => Outcome(edit(0, invincibility))
    case _                     => Outcome(this)
  }

  override def update(context: FrameContext[StartupData])(room: RoomModel): Outcome[Model] =
    for {
      superObj <- super.update(context)(room)
      newObj = invincibilityTimer match {
        case 0 => superObj
        case _ if invincibilityTimer - context.gameTime.delta.toDouble > 0 =>
          superObj.edit(life, invincibilityTimer - context.gameTime.delta.toDouble).asInstanceOf[Model]
        case _ => superObj.edit(life, 0).asInstanceOf[Model]
      }
    } yield newObj

}
