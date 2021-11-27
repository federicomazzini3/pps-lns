package lns.scenes.game.subsystems

import indigo.shared.events.GlobalEvent
import indigo.shared.subsystems.SubSystemFrameContext
import indigo.shared.time.GameTime
import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.characters.CharacterModel
import indigo.*

case class BattleModel(bc: Set[BattleConsequence], state: GameState) {
  def update(f: BattleModel => Outcome[BattleModel]): Outcome[BattleModel] = f(this)

  def expireBattleConsequence(context: SubSystemFrameContext): Outcome[BattleModel] =
    this.update(battleModel =>
      Outcome(battleModel.copy(bc = bc.filter(bc => bc.timeElapse(context.gameTime, Seconds(0.360)))))
    )

  def addBattleConsequence(toAddBC: BattleConsequence): Outcome[BattleModel] =
    this.update(battleModel => Outcome(this.copy(bc = bc + toAddBC)))

  def gameOver: Outcome[BattleModel] =
    this.update(battleModel => Outcome(this.copy(state = GameOver))).addGlobalEvents(Redirect(500))

  def win: Outcome[BattleModel] = Outcome(this.copy(state = Win))
}

object BattleModel {
  def initial = BattleModel(Set.empty, Play)
}

trait BattleConsequence {
  val a: AnythingModel
  val gameTime: GameTime

  def timeElapse(other: GameTime, seconds: Seconds): Boolean =
    other.running.-(gameTime.running) < seconds
}
case class Hurt(a: AnythingModel, gameTime: GameTime) extends BattleConsequence
case class Rip(a: AnythingModel, gameTime: GameTime)  extends BattleConsequence

trait GameState
case object Win      extends GameState
case object GameOver extends GameState
case object Play     extends GameState

trait BattleEvent                 extends GlobalEvent { val a: AnythingModel }
case class Hit(a: AnythingModel)  extends BattleEvent
case class Dead(a: AnythingModel) extends BattleEvent

case class Redirect(frameRemaining: Int) extends GlobalEvent
case object ResetSubsystem               extends GlobalEvent
