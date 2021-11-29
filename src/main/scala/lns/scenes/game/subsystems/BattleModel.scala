package lns.scenes.game.subsystems

import indigo.shared.events.GlobalEvent
import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.time.GameTime
import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.characters.CharacterModel
import indigo.*

/**
 * Model for BattleEventSubsystem
 * @param bc
 *   collection of battleconsequence (hurt or dead for an anything)
 * @param state
 *   state of the game
 */
case class BattleModel(bc: Set[BattleConsequence], state: GameState) {

  /**
   * Update a battle model
   * @param f
   *   strategy of update
   * @return
   *   a new updated BattleModel
   */
  def update(f: BattleModel => Outcome[BattleModel]): Outcome[BattleModel] = f(this)

  /**
   * Remove all BattleConsequence older than 360 milliseconds ago.
   * @param context
   *   the context of subsystem that store GameTime
   * @return
   *   a new updated BattleModel
   */
  def expireBattleConsequence(context: SubSystemFrameContext): Outcome[BattleModel] =
    this.update(battleModel =>
      Outcome(battleModel.copy(bc = bc.filter(bc => bc.timeElapse(context.gameTime, Seconds(0.360)))))
    )

  /**
   * Add a BattleConsequence to the collection
   * @param toAddBC
   *   the BattleConsequence to add
   * @return
   *   a new updated BattleModel
   */
  def addBattleConsequence(toAddBC: BattleConsequence): Outcome[BattleModel] =
    this.update(battleModel => Outcome(this.copy(bc = bc + toAddBC)))

  /**
   * Update the state of BattleModel with game over
   * @return
   *   a new updated BattleModel
   */
  def gameOver: Outcome[BattleModel] =
    this.update(battleModel => Outcome(this.copy(state = GameOver))).addGlobalEvents(Redirect(500))

  /**
   * Update the state of BattleModel with win
   * @return
   *   a new updated BattleModel
   */
  def win: Outcome[BattleModel] =
    this.update(battleModel => Outcome(this.copy(state = Win))).addGlobalEvents(Redirect(500))
}

/**
 * Companion object of BattleModel
 */
object BattleModel {
  def initial = BattleModel(Set.empty, Play)
}

/**
 * Trait for a battle consequence. A battle consequence is a consequence of a shot that it an Anything That can be a
 * Hurt or a Dead
 */
trait BattleConsequence {
  val a: AnythingModel
  val gameTime: GameTime

  /**
   * Time between the creation of this BattleConsequence and another moment
   * @param other
   *   the other time to compare
   * @param seconds
   *   the seconds between the two to compare
   * @return
   */
  def timeElapse(other: GameTime, seconds: Seconds): Boolean =
    other.running.-(gameTime.running) < seconds
}

case class Hurt(a: AnythingModel, gameTime: GameTime) extends BattleConsequence
case class Rip(a: AnythingModel, gameTime: GameTime)  extends BattleConsequence

/**
 * Trait that model the state of the game A game can be
 *   - in playing
 *   - Win
 *   - Game over
 */
trait GameState
case object Win      extends GameState
case object GameOver extends GameState
case object Play     extends GameState

/**
 * Event that BattleModel and his subsystem manage
 */
trait BattleEvent                 extends GlobalEvent { val a: AnythingModel }
case class Hit(a: AnythingModel)  extends BattleEvent
case class Dead(a: AnythingModel) extends BattleEvent

case class Redirect(frameRemaining: Int) extends GlobalEvent
case object ResetSubsystem               extends GlobalEvent
