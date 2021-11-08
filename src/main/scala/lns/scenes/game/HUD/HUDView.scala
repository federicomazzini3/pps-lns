package lns.scenes.game.HUD

import scala.language.implicitConversions
import indigo.*
import indigo.shared.FrameContext
import indigo.shared.datatypes.Vector2
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.StartupData
import lns.core.Assets
import lns.scenes.game.character.CharacterModel
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

object HUDView {
  val width  = 100
  val height = 200

  enum HeartStatus:
    case Full, Empty, Half
  import HeartStatus.*

  def computeLife(maxlife: Int, life: Double): List[HeartStatus] =
    (1 to maxlife).toList
      .foldLeft((List.empty[HeartStatus], life))((acc, i) =>
        acc match {
          case (l, current) if current <= 0 => (l :+ Empty, current - 1)
          case (l, current) if current < 1  => (l :+ Half, current - 1)
          case (l, current)                 => (l :+ Full, current - 1)
        }
      )
      ._1

  def drawLife(character: CharacterModel): Group =
    Group().addChildren(
      computeLife(MaxLife @@ character.stats, character.life).zipWithIndex.map {
        case (Full, count)  => Assets.HUD.Graphics.heartFull.moveTo(count * Assets.HUD.iconWidth, 0)
        case (Half, count)  => Assets.HUD.Graphics.heartHalf.moveTo(count * Assets.HUD.iconWidth, 0)
        case (Empty, count) => Assets.HUD.Graphics.heartEmpty.moveTo(count * Assets.HUD.iconWidth, 0)
      }
    )

  def drawStatsText(msg: String, x: Int, y: Int): Text[Material.ImageEffects] =
    Text(msg, x, y, 1, Assets.Fonts.fontKey, Assets.Fonts.fontMaterial)

  def drawStats(character: CharacterModel): Group =
    Group()
      .addChild(Assets.HUD.Graphics.MaxSpeed.moveTo(0, 40))
      .addChild(drawStatsText(MaxSpeed @@ character.stats, 30, 46))
      .addChild(Assets.HUD.Graphics.FireDamage.moveTo(0, 70))
      .addChild(drawStatsText(FireDamage @@ character.stats, 30, 76))
      .addChild(Assets.HUD.Graphics.FireRange.moveTo(0, 100))
      .addChild(drawStatsText(FireRange @@ character.stats, 30, 106))
      .addChild(Assets.HUD.Graphics.FireRate.moveTo(0, 130))
      .addChild(drawStatsText(FireRate @@ character.stats, 30, 136))

  def draw(context: FrameContext[StartupData], character: CharacterModel): Group =
    Group()
      .addChild(drawLife(character))
      .addChild(drawStats(character))
      .moveTo(10, 10)
}
