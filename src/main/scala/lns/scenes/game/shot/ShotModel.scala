package lns.scenes.game.shot

import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.{ AliveModel, DamageModel, DynamicModel }
import lns.scenes.game.room.RoomModel
import org.scalajs.dom.raw.Position

case class ShotModel(
    boundingBox: BoundingBox,
    speed: Vector2,
    maxSpeed: Vector2,
    direction: Vector2,
    damage: Double,
    range: Double,
    life: Int = 1,
    invincibilityTimer: Double = 0
) extends AliveModel
    with DynamicModel
    with DamageModel {

  type Model = ShotModel
  val invincibility: Double = 0

  def withAlive(life: Int, invincibilityTimer: Double): Model      = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2): Model = copyMacro
  def withDamage(damage: Double): Model                            = copyMacro

  def computeSpeed(context: FrameContext[StartupData]): Vector2 =
    maxSpeed * direction
}

case class ShotEvent(position: Vertex, direction: Vector2) extends GlobalEvent

object ShotModel {
  def apply(position: Vertex, direction: Vector2): ShotModel = ShotModel(
    BoundingBox(
      position,
      Vertex(5, 5)
    ),
    Vector2(0, 0),
    Vector2(800, 800),
    direction,
    10,
    500
  )

  def updateShots(shots: List[ShotModel])(context: FrameContext[StartupData])(room: RoomModel): List[ShotModel] =
    shots.map(shot => shot.update(context)(room).unsafeGet)
}
