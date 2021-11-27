package lns.scenes.game.subsystems

import indigo.*
import indigo.scenes.SceneEvent.JumpTo
import indigo.shared.datatypes.Vector2
import lns.core.Assets
import lns.scenes.end.*
import lns.scenes.end.Restart
import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.*
import lns.scenes.game.bosses.BossModel
import lns.scenes.game.characters.CharacterModel
import lns.scenes.game.enemies.EnemyModel
import lns.scenes.game.shots.ShotModel

case class BattleEventSubSystems(screenDimensions: Rectangle) extends SubSystem:
  type EventType      = GlobalEvent
  type SubSystemModel = BattleModel

  val eventFilter: GlobalEvent => Option[EventType] = {
    case e: FrameTick   => Option(e)
    case e: Hit         => Option(e)
    case e: Dead        => Option(e)
    case e: Redirect    => Option(e)
    case ResetSubsystem => Option(ResetSubsystem)
    case _              => None
  }

  def initialModel: Outcome[SubSystemModel] =
    Outcome(BattleModel.initial)

  def update(context: SubSystemFrameContext, model: SubSystemModel): GlobalEvent => Outcome[SubSystemModel] = {

    case FrameTick => model.expireBattleConsequence(context)

    case Hit(a) => Outcome(model)

    case Dead(a) =>
      a match {
        case c: CharacterModel =>
          for {
            gameOver <- model.gameOver
            bc       <- gameOver.addBattleConsequence(Rip(a, context.gameTime))
          } yield bc
        case b: BossModel =>
          for {
            win <- model.win
            bc  <- win.addBattleConsequence(Rip(a, context.gameTime))
          } yield bc
        case _ =>
          model.addBattleConsequence(Rip(a, context.gameTime))
      }

    case Redirect(i) =>
      i match {
        case 0 => Outcome(model).addGlobalEvents(JumpTo(EndScene.name))
        case _ => Outcome(model).addGlobalEvents(Redirect(i - 1))
      }

    case ResetSubsystem => Outcome(BattleModel.initial)

    case _ => Outcome(model)
  }

  def present(context: SubSystemFrameContext, model: SubSystemModel): Outcome[SceneUpdateFragment] = Outcome(
    SceneUpdateFragment.empty
      .addLayers(
        Layer(
          BindingKey("battle_consequences"),
          BattleEventView
            .draw(model)
            .withScale(Vector2(BattleEventView.scale(screenDimensions, Assets.Rooms.roomSize)))
            .withRef(Assets.Rooms.roomSize / 2, Assets.Rooms.roomSize / 2)
            .moveTo(screenDimensions.center)
        )
      )
  )
