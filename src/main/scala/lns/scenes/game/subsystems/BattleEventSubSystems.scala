package lns.scenes.game.subsystems

import indigo.*
import indigo.shared.datatypes.Vector2
import lns.core.Assets
import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.*
import lns.scenes.game.shot.ShotModel

case class BattleEventSubSystems(screenDimensions: Rectangle) extends SubSystem:
  type EventType      = GlobalEvent
  type SubSystemModel = Set[BattleConsequence]

  val eventFilter: GlobalEvent => Option[EventType] = {
    case e: FrameTick => Option(e)
    case e: Hit       => Option(e)
    case e: Dead      => Option(e)
    case _            => None
  }

  def initialModel: Outcome[SubSystemModel] =
    Outcome(Set.empty)

  def update(context: SubSystemFrameContext, model: SubSystemModel): GlobalEvent => Outcome[SubSystemModel] = {

    case FrameTick =>
      Outcome(
        model
          .filter(bc => bc.timeElapse(context.gameTime, Seconds(0.360)))
      )

    case Hit(a) => Outcome(model)

    case Dead(a) => Outcome(model + Rip(a, context.gameTime))

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

object BattleEventView {

  def scale(screenDimensions: Rectangle, edge: Int): Double =
    Math.min(
      1.0 / edge * screenDimensions.width,
      1.0 / edge * screenDimensions.height
    )

  def draw(model: Set[BattleConsequence]): Group =
    model
      .foldLeft(Group())((e1, e2) =>
        e1.addChild(
          anymation(e2)
        )
      )

  def anymation(bc: BattleConsequence) =
    bc match {
      case Rip(a, _) =>
        a match {
          case _ => ripAnymation(bc)
        }
      case h: Hurt => Group()
    }

  def ripAnymation(bc: BattleConsequence) =
    Sprite(
      BindingKey("explosion_animation_sprite" + bc.gameTime.running),
      bc.a.boundingBox.topLeft.x.toInt,
      bc.a.boundingBox.topLeft.y.toInt,
      1,
      AnimationKey("explosion_animation"),
      Material.Bitmap(AssetName("explosion"))
    )
      .play()
      .withRef(32, 32)
      .moveBy(Assets.Rooms.wallSize, Assets.Rooms.wallSize)
      .withScale(adHocScale(bc))

  def adHocScale(bc: BattleConsequence): Vector2 =
    bc.a match {
      case s: ShotModel => Vector2(2, 2)
      case _            => Vector2(5, 5)
    }
}

trait BattleEvent                 extends GlobalEvent { val a: AnythingModel }
case class Hit(a: AnythingModel)  extends BattleEvent
case class Dead(a: AnythingModel) extends BattleEvent

trait BattleConsequence {
  val a: AnythingModel
  val gameTime: GameTime

  def timeElapse(other: GameTime, seconds: Seconds): Boolean =
    other.running.-(gameTime.running) < seconds
}
case class Hurt(a: AnythingModel, gameTime: GameTime) extends BattleConsequence
case class Rip(a: AnythingModel, gameTime: GameTime)  extends BattleConsequence
