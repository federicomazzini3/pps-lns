package lns.scenes.game.bosses

import indigo.*
import indigo.shared.{ FrameContext, Outcome }
import indigoextras.geometry.{ BoundingBox, Vertex }
import lns.StartupData
import lns.core.{ Assets, PrologClient }
import lns.core.Macros.copyMacro
import lns.scenes.game.GameContext
import lns.scenes.game.anything.{ *, given }
import lns.scenes.game.enemies.*
import lns.scenes.game.elements.*
import lns.scenes.game.shots.{ ShotRed, SingleShotView }
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*
import lns.subsystems.prolog.Substitution

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
 * @param shot
 *   [[FireModel]] shot, default None
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
    shot: Option[Vector2] = None,
    shots: Option[List[Vector2]] = None,
    path: Queue[Vector2] = Queue.empty,
    prologClient: PrologClient = PrologClient()
) extends EnemyModel
    with DynamicModel
    with FireModel
    with MultiFireModel
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
  def withFire(fireRateTimer: Double, shot: Option[Vector2]): Model                            = copyMacro
  def withMultiFire(fireRateTimer: Timer, shots: Option[List[Vector2]]): Model                 = copyMacro
  def withSolid(crossable: Boolean): Model                                                     = copyMacro
  def withProlog(prologClient: PrologClient): Model                                            = copyMacro

  /**
   * Builds boss info for goal
   */
  def bossInfo: String =
    val x: Int = getPosition().x / Assets.Rooms.cellSize
    val y: Int = getPosition().y / Assets.Rooms.cellSize
    "boss(" + x + "," + y + "," + life + "," + MaxLife @@ stats + ")"

  /**
   * Builds character info for goal based on current gameContext
   * @param [[GameContext]]
   */
  def characterInfo(gameContext: GameContext): String =
    val x: Int = gameContext.character.getPosition().x / Assets.Rooms.cellSize
    val y: Int = gameContext.character.getPosition().y / Assets.Rooms.cellSize
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
      .foldLeft(List[String]()) {
        case (list, (id, stone: StoneModel)) =>
          val x = stone.getPosition().x / Assets.Rooms.cellSize
          val y = stone.getPosition().y / Assets.Rooms.cellSize
          list :+ "block(" + x + "," + y + ")"
        case (list, _) => list
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
   * Set the [[EnemyState]] based on prolog result:
   * @param state
   *   [[EnemyState]]
   * @param timer
   *   state time
   * @param option
   *   Option[Any] optional parameter for state
   * @return
   *   the Outcome of the updated model
   */
  def behaviourOutcome(state: EnemyState, timer: Timer, option: Option[Any]): Outcome[Model] =
    Outcome(this.withStatus((state, timer, option) +: status.drop(1)))

  /**
   * Implements model behaviour
   * @param response
   *   [[Substitution]] PrologClient consult result
   * @return
   *   the Outcome of the updated model
   */
  override def behaviour(response: Substitution): Outcome[Model] =
    val attackRegEx  = raw"attack1\(([a-z]+)\)".r
    val moveRegEx    = raw"move\((\d{1}),\s*(\d{1})\)".r
    val defenceRegEx = raw"defence\((\d{1}),\s*(\d{1})\)".r

    response.links("Action").toString() match {
      case attackRegEx(direction) =>
        behaviourOutcome(EnemyState.Attacking, FireRate @@ stats, Some(("attack1", direction)))
      case "attack2" =>
        behaviourOutcome(EnemyState.Attacking, FireRate @@ stats, Some("attack2"))
      case "attack3" =>
        behaviourOutcome(EnemyState.Attacking, FireRate @@ stats, Some("attack3"))
      case moveRegEx(x, y) =>
        behaviourOutcome(EnemyState.Attacking, 0, Some("move", (x.toDouble, y.toDouble)))
      case defenceRegEx(x, y) =>
        Outcome(
          this
            .withSolid(true)
            .withStatus(
              (EnemyState.Hiding, 1.0, Some("defence", (x.toDouble, y.toDouble))) :+
                (EnemyState.Falling, 1.0, None) :+
                (EnemyState.Idle, 0, None)
            )
        )
      case _ => Outcome(this)
    }

  override def computeFire(context: FrameContext[StartupData])(gameContext: GameContext): Option[Vector2] =
    status.head match {
      case (EnemyState.Attacking, _, Some(("attack1", direction))) =>
        direction match {
          case "top"   => Some(Vector2(0, -1))
          case "right" => Some(Vector2(1, 0))
          case "down"  => Some(Vector2(0, 1))
          case "left"  => Some(Vector2(-1, 0))
          case _       => None
        }
      case _ => None
    }

  override def computeMultiFire(context: FrameContext[StartupData])(gameContext: GameContext): Option[List[Vector2]] =
    status.head match {
      case (EnemyState.Attacking, _, Some("attack2")) =>
        Some(List(Vector2(0, -1), Vector2(1, 0), Vector2(0, 1), Vector2(-1, 0)))
      case (EnemyState.Attacking, _, Some("attack3")) =>
        Some(List(Vector2(-1, -1), Vector2(1, 1), Vector2(1, -1), Vector2(-1, 1)))
      case _ => None
    }

  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    println(status.head)
    for {
      superObj <- super.update(context)(gameContext)
      newObj = status.head match {
        case (EnemyState.Attacking, time, Some(("attack1", direction))) =>
          superObj.withStatus((EnemyState.Attacking, time, None) +: status.drop(1))
        case (EnemyState.Attacking, time, Some("attack2")) =>
          superObj.withStatus((EnemyState.Attacking, time, None) +: status.drop(1))
        case (EnemyState.Attacking, time, Some("attack3")) =>
          superObj.withStatus((EnemyState.Attacking, time, None) +: status.drop(1))
        case (EnemyState.Attacking, _, Some(("move", (x: Double, y: Double)))) =>
          superObj
            .withStatus((EnemyState.Attacking, 0, None) +: status.drop(1))
            .withTraveller(Queue(Vector2(x * Assets.Rooms.cellSize, y * Assets.Rooms.cellSize)))
            .asInstanceOf[Model]
        case (EnemyState.Hiding, 0, Some(("defence", (x: Double, y: Double)))) =>
          superObj
            .withDynamic(boundingBox.moveTo(x * Assets.Rooms.cellSize, y * Assets.Rooms.cellSize), Vector2.zero, false)
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
