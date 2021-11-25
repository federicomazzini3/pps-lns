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
    status: Queue[EnemyStatus] = Queue((EnemyState.Idle, 0)),
    crossable: Boolean = false,
    speed: Vector2 = Vector2(0, 0),
    collisionDetected: Boolean = false,
    life: Double = 0,
    invincibilityTimer: Timer = 0,
    fireRateTimer: Timer = 0,
    shot: Option[Vector2] = None,
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
  def withFire(fireRateTimer: Double, shot: Option[Vector2]): Model                            = copyMacro
  def withSolid(crossable: Boolean): Model                                                     = copyMacro
  def withProlog(prologClient: PrologClient): Model                                            = copyMacro

  def computeFire(context: FrameContext[StartupData])(gameContext: GameContext): Option[Vector2] = None

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
   * behaviour(boss(1,1,4,10),character(1,4,10,10),room(9,9),[block(5,5),block(5,6)],A).
   */
  override def goal(context: FrameContext[StartupData])(gameContext: GameContext): String =
    "behaviour(" +
      bossInfo + "," +
      characterInfo(gameContext) + "," +
      roomInfo + "," +
      blocksInfo(gameContext) + ", A)."

  override def behaviour(response: Substitution): Outcome[Model] =
    println("RESPONSE")
    println(response)
    Outcome(this.withStatus((EnemyState.Attacking, 5) +: status.drop(1)))

  /*
  override def update(context: FrameContext[StartupData])(gameContext: GameContext): Outcome[Model] =
    for {
      superObj <- super.update(context)(gameContext)
      newObj = status.head match {
        case (EnemyState.Idle, 0) if getPosition().distanceTo(gameContext.character.getPosition()) >= 10 =>
          superObj
            .withTraveller(
              Queue(gameContext.character.getPosition().clamp(0, Assets.Rooms.floorSize - boundingBox.height))
            )
            .withStatus((EnemyState.Attacking, 0.0))
            .asInstanceOf[Model]
        case (EnemyState.Idle, _) if crossable == true => superObj.withSolid(false)
        case (EnemyState.Attacking, _) if superObj.path.isEmpty == true =>
          superObj.withSolid(true).withStatus((EnemyState.Hiding, 2.0) :+ (EnemyState.Idle, 1.0)).asInstanceOf[Model]
        case _ => superObj

      }
    } yield newObj
   */
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
