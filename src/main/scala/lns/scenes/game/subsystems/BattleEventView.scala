package lns.scenes.game.subsystems

import indigo.*
import indigo.shared.assets.AssetName
import indigo.shared.datatypes.{ Rectangle, Vector2 }
import indigo.shared.materials.Material
import indigo.shared.scenegraph.{ Group, Sprite }
import lns.core.Assets
import lns.scenes.game.shots.ShotModel
import lns.scenes.game.subsystems.*

object BattleEventView {

  def draw(battleModel: BattleModel): Group =
    Group()
      .addChild(drawAnymation(battleModel))
      .addChild(drawState(battleModel))

  def drawAnymation(battleModel: BattleModel): Group =
    def anymation(bc: BattleConsequence) =
      bc match {
        case Rip(a, _) => ripAnymation(bc)
        case h: Hurt   => Group()
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

    battleModel.bc.foldLeft(Group())((e1, e2) => e1.addChild(anymation(e2)))

  def drawState(battleModel: BattleModel): Group =
    def stateMessage(battleModel: BattleModel): Option[String] = battleModel.state match {
      case GameOver => Some("GAME OVER!")
      case Win      => Some("YOU WIN!")
      case _        => None
    }
    stateMessage(battleModel) match {
      case Some(message) =>
        Group()
          .addChild(
            Text(
              message,
              Assets.Rooms.roomSize / 2,
              Assets.Rooms.roomSize / 2,
              1,
              Assets.Fonts.fontKey,
              Assets.Fonts.fontMaterial
            ).alignCenter.withScale(Vector2(8, 8))
          )
      case _ => Group()
    }

  def scale(screenDimensions: Rectangle, edge: Int): Double =
    Math.min(
      1.0 / edge * screenDimensions.width,
      1.0 / edge * screenDimensions.height
    )

  def adHocScale(bc: BattleConsequence): Vector2 =
    bc.a match {
      case s: ShotModel => Vector2(2, 2)
      case _            => Vector2(5, 5)
    }
}
