package lns.scenes.game.enemy

import indigo.*
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Assets
import lns.core.Macros.copyMacro
import lns.scenes.game.anything.*
import lns.scenes.game.room.{ Boundary, RoomModel }
import lns.scenes.game.shot.ShotEvent
import lns.scenes.game.stats.*
import lns.scenes.game.stats.PropertyName.*

import scala.language.implicitConversions

enum EnemyState:
  case Idle, Attacking, Defending, Hiding

/**
 * Enemy model trait that is alive and make damage. An Enemy has a status to represent its intentions
 */
trait EnemyModel extends AliveModel with DamageModel {
  type Model >: this.type <: EnemyModel

  val status: EnemyState

  def withStatus(status: EnemyState): Model
}
