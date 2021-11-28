package lns.scenes.game.bosses

import indigo.*
import indigo.shared.{ FrameContext, Outcome }
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.Animations.Loki
import lns.core.{ Assets, PrologClient }
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ *, given }
import lns.scenes.game.enemies.{ *, given }
import lns.scenes.game.elements.*
import lns.scenes.game.shots.{ ShotRed, SingleShotView }
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*
import lns.subsystems.prolog.Substitution
import lns.subsystems.prolog.Term.*

import scala.collection.immutable.Queue
import scala.language.implicitConversions

/**
 * Boss model that is alive, it's dynamic by computing its speed and new position by a defined strategy, can fire
 * computing shot and have stats. The Behaviour of Boss is defined by Prolog
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
 * @param status
 *   Initial [[EnemyState]]
 * @param crossable
 *   crossable, default false
 * @param speed
 *   [[DynamicModel]] speed, default Vector2(0, 0)
 * @param collisionDetected
 *   [[DynamicModel]] collisionDetected, true if the Anything is collided with some other Anything. Default false
 * @param life
 *   [[AliveModel]] life, default 0
 * @param invincibilityTimer
 *   [[AliveModel]] invincibilityTimer, default 0
 * @param fireRateTimer
 *   [[FireModel]] fireRateTimer, default 0
 * @param shots
 *   [[FireModel]] shots, default None
 * @param path
 *   [[Traveller]] path, default Queue.empty
 * @param prologClient
 *   [[PrologModel]] default prologClient
 */
case class BossModel(
    id: AnythingId,
    view: () => BossView[_],
    boundingBox: BoundingBox,
    shotAreaOffset: Int,
    stats: Stats,
    status: Queue[EnemyStatus] = Queue((EnemyState.Idle, 0, None)),
    crossable: Boolean = false,
    speed: Vector2 = Vector2(0, 0),
    collisionDetected: Boolean = false,
    life: Double = 0,
    invincibilityTimer: Timer = 0,
    fireRateTimer: Timer = 0,
    shots: Option[List[Vector2]] = None,
    path: Queue[Vector2] = Queue.empty,
    prologClient: PrologClient = PrologClient()
) extends EnemyModel
    with DynamicModel
    with FireModel
    with Traveller
    with PrologModel("loki") {

  type Model = BossModel

  val shotView   = () => new SingleShotView() with ShotRed
  val shotOffset = boundingBox.height / 2

  def withStats(stats: Stats): Model                                                           = copyMacro
  def withStatus(status: Queue[EnemyStatus]): Model                                            = copyMacro
  def withAlive(life: Double, invincibilityTimer: Double): Model                               = copyMacro
  def withDynamic(boundingBox: BoundingBox, speed: Vector2, collisionDetected: Boolean): Model = copyMacro
  def withTraveller(path: Queue[Vector2]): Model                                               = copyMacro
  def withFire(fireRateTimer: Double, shots: Option[List[Vector2]]): Model                     = copyMacro
  def withSolid(crossable: Boolean): Model                                                     = copyMacro
  def withProlog(prologClient: PrologClient): Model                                            = copyMacro

  /**
   * Builds boss info for goal
   */
  def bossInfo: String =
    val x: Int = Assets.Rooms.positionToCell(getPosition().x)
    val y: Int = Assets.Rooms.positionToCell(getPosition().y)
    "boss(" + x + "," + y + "," + life + "," + MaxLife @@ stats + ")"

  /**
   * Builds character info for goal based on current gameContext
   * @param [[GameContext]]
   */
  def characterInfo(gameContext: GameContext): String =
    val x: Int = Assets.Rooms.positionToCell(gameContext.character.getPosition().x)
    val y: Int = Assets.Rooms.positionToCell(gameContext.character.getPosition().y)
    "character(" + x + "," + y + "," + gameContext.character.life + "," + MaxLife @@ gameContext.character.stats + ")"

  /**
   * Builds room size info for goal
   */
  def roomInfo: String =
    val size: Int = Assets.Rooms.floorSize / Assets.Rooms.cellSize
    "room(" + size + "," + size + ")"

  /**
   * Builds blocks info for goal based on current gameContext: a list of all stone insede room
   * @param [[GameContext]]
   */
  def blocksInfo(gameContext: GameContext): String =
    gameContext.room.anythings
      .collect { case (_, stone: ElementModel) =>
        stone
      }
      .foldLeft(List[String]()) { (list, stone) =>
        list :+ "block(" +
          Assets.Rooms.positionToCell(stone.getPosition().x) + "," +
          Assets.Rooms.positionToCell(stone.getPosition().y) + ")"
      }
      .mkString("[", ",", "]")

  /**
   * Builds the goal string for the prolog example:
   * behaviour(boss(1,1,4,10),character(1,4,10,10),room(9,9),[block(5,5),block(5,6)], Action).
   */
  override def goal(context: FrameContext[StartupData])(gameContext: GameContext): String =
    "behaviour(" +
      bossInfo + "," +
      characterInfo(gameContext) + "," +
      roomInfo + "," +
      blocksInfo(gameContext) + ", Action)."

  /**
   * Update single [[EnemyState]] based on prolog result:
   * @param state
   *   [[EnemyState]]
   * @param timer
   *   state time
   * @param option
   *   Option[Any] optional parameter for state
   * @return
   *   the Outcome of the updated model
   */
  def behaviourOutcome(state: EnemyState, timer: Timer, option: Option[EnemyAction]): Outcome[Model] =
    Outcome(this.withStatus((state, timer, option) +: (EnemyState.Idle, 0.0, None)))

  /**
   * Implements model behaviour
   * @param response
   *   [[Substitution]] PrologClient consult result
   * @return
   *   the Outcome of the updated model
   */
  override def behaviour(response: Substitution): Outcome[Model] =
    response.links("Action") match {
      case Struct(Atom("attack1"), Atom(direction)) =>
        behaviourOutcome(EnemyState.Attacking, FireRate @@ stats, Some(AttackAction("attack1", Some(direction))))
      case Atom("attack2") =>
        behaviourOutcome(EnemyState.Attacking, FireRate @@ stats, Some(AttackAction("attack2", None)))
      case Atom("attack3") =>
        behaviourOutcome(EnemyState.Attacking, FireRate @@ stats, Some(AttackAction("attack3", None)))
      case Struct(Atom("move"), Num(x, _), Num(y, _)) =>
        Outcome(this.withStatus(EnemyState.Attacking, 0.0, Some(MoveAction(x.toDouble, y.toDouble))))
      case Struct(Atom("defence"), Num(x, _), Num(y, _)) =>
        Outcome(
          this
            .withSolid(true)
            .withStatus(
              (EnemyState.Hiding, Loki.hidingTime, Some(DefenceAction(x.toDouble, y.toDouble))) +:
                (EnemyState.Falling, Loki.fallingTime, None) +:
                (EnemyState.Idle, 0.0, None)
            )
        )
      case _ => Outcome(this)
    }

  override def computeFire(context: FrameContext[StartupData])(gameContext: GameContext): Option[List[Vector2]] =
    status.head match {
      case (EnemyState.Attacking, _, Some(AttackAction("attack1", Some(direction)))) =>
        direction match {
          case "top"   => Some(List(Vector2(0, -1)))
          case "right" => Some(List(Vector2(1, 0)))
          case "down"  => Some(List(Vector2(0, 1)))
          case "left"  => Some(List(Vector2(-1, 0)))
          case _       => None
        }
      case (EnemyState.Attacking, _, Some(AttackAction("attack2", None))) =>
        Some(List(Vector2(0, -1), Vector2(1, 0), Vector2(0, 1), Vector2(-1, 0)))
      case (EnemyState.Attacking, _, Some(AttackAction("attack3", None))) =>
        Some(List(Vector2(-1, -1), Vector2(1, 1), Vector2(1, -1), Vector2(-1, 1)))
      case _ => None
    }

  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    for {
      superObj <- super.update(context)(gameContext)
      newObj = status.head match {
        case (EnemyState.Attacking, _, Some(MovingAction())) if superObj.path.length == 0 =>
          superObj.withStatus((EnemyState.Idle, 0.0, None)).asInstanceOf[Model]
        case (EnemyState.Attacking, _, Some(MoveAction(x, y))) =>
          superObj
            .withTraveller(Queue(Vector2(Assets.Rooms.cellToPosition(x), Assets.Rooms.cellToPosition(y))))
            .withStatus((EnemyState.Attacking, 0.0, Some(MovingAction())))
            .asInstanceOf[Model]
        case (EnemyState.Hiding, 0, Some(DefenceAction(x, y))) =>
          superObj
            .withDynamic(
              boundingBox.moveTo(Assets.Rooms.cellToPosition(x), Assets.Rooms.cellToPosition(y)),
              Vector2.zero,
              false
            )
            .asInstanceOf[Model]
        case (EnemyState.Falling, 0, _) =>
          superObj
            .withSolid(false)
            .withDynamic(boundingBox, Vector2.zero, true)
            .asInstanceOf[Model]
        case _ => superObj
      }
    } yield newObj

}

/**
 * [[EnemyAction]] implements for [[BossModel]]
 */
case class AttackAction(val name: String, direction: Option[String]) extends EnemyAction
case class MoveAction(x: Double, y: Double)                          extends EnemyAction
case class MovingAction()                                            extends EnemyAction
case class DefenceAction(x: Double, y: Double)                       extends EnemyAction

/**
 * Factory of [[BossModel]]
 */
object BossModel {

  def initial: BossModel = BossModel(
    AnythingId.generate,
    view = () => BossView,
    boundingBox =
      BossView.boundingBox(Vertex(Assets.Rooms.floorSize / 2, Assets.Rooms.floorSize / 2 + Assets.Rooms.floorSize / 4)),
    shotAreaOffset = BossView.shotAreaOffset,
    stats = Stats.Loki,
    life = MaxLife @@ Stats.Loki
  )
}
