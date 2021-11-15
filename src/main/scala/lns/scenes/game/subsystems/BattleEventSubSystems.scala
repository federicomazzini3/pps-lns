package lns.scenes.game.subsystems

import indigo.*
import indigo.shared.datatypes.Vector2
import lns.core.Assets
import lns.scenes.game.anything.AnythingModel
import lns.scenes.game.*

case class BattleEventSubSystems(screenDimensions: Rectangle) extends SubSystem:
  type EventType      = GlobalEvent
  type SubSystemModel = Set[BattleEvent]

  val eventFilter: GlobalEvent => Option[EventType] = {
    case e: Hit  => Option(e)
    case e: Dead => Option(e)
    case _       => None
  }

  def initialModel: Outcome[SubSystemModel] =
    Outcome(Set.empty)

  def update(context: SubSystemFrameContext, model: SubSystemModel): GlobalEvent => Outcome[SubSystemModel] = {
    case Hit(a)  => println("HIT " + a); Outcome(model + Hit(a))
    case Dead(a) => println("DEAD " + a); Outcome(model + Dead(a))
    case _       => Outcome(model)
  }

  def present(context: SubSystemFrameContext, model: SubSystemModel): Outcome[SceneUpdateFragment] = Outcome(
    SceneUpdateFragment.empty
      .addLayers(
        Layer(
          BindingKey("game"),
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

  def draw(model: Set[BattleEvent]): Group = model.foldLeft(Group())((e1, e2) =>
    e1.addChild(
      Sprite(
        BindingKey("nerve_body_sprite"),
        e2.a.boundingBox.position.x.toInt,
        e2.a.boundingBox.position.y.toInt,
        1,
        AnimationKey("nerve_body"),
        Material.Bitmap(AssetName("nerve"))
      ).changeCycle(CycleLabel("idle"))
        .play()
        .moveBy(Assets.Rooms.wallSize, Assets.Rooms.wallSize)
    )
  )
}

trait BattleEvent                 extends GlobalEvent { val a: AnythingModel }
case class Hit(a: AnythingModel)  extends BattleEvent
case class Dead(a: AnythingModel) extends BattleEvent
