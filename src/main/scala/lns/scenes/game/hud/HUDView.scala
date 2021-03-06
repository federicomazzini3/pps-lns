package lns.scenes.game.hud

import scala.language.implicitConversions
import indigo.*
import indigo.shared.FrameContext
import indigo.shared.datatypes.Vector2
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.StartupData
import lns.core.Assets
import lns.scenes.game.characters.CharacterModel
import lns.scenes.game.stats.{ *, given }
import lns.scenes.game.stats.PropertyName.*

/**
 * HUD View
 */
object HUDView {
  val width  = 100
  val height = 200

  enum HeartStatus:
    case Full, Empty, Half
  import HeartStatus.*

  /**
   * Method to compute how many full, half and empty hearts show. Cased on the current life in relation to the maximum
   * life
   * @param maxlife
   *   Maxlife
   * @param life
   *   Current life
   * @return
   *   List[HeartStatus] enum: Full, Empty Half
   */
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

  /**
   * Draw the computed life with correct Heart asset and position on screen
   * @param character
   *   [[CharacterModel]] to get max life and current life
   * @return
   *   Group
   */
  def drawLife(character: CharacterModel): Group =
    Group().addChildren(
      computeLife(MaxLife @@ character.stats, character.life).zipWithIndex.map {
        case (Full, count)  => Assets.HUD.Graphics.heartFull.moveTo(count * Assets.HUD.iconWidth, 0)
        case (Half, count)  => Assets.HUD.Graphics.heartHalf.moveTo(count * Assets.HUD.iconWidth, 0)
        case (Empty, count) => Assets.HUD.Graphics.heartEmpty.moveTo(count * Assets.HUD.iconWidth, 0)
      }
    )

  /**
   * Draw the stats value as text
   * @param msg
   *   String of stats value
   * @param x
   *   X position int he HUD view
   * @param y
   *   Y position int he HUD view
   * @return
   *   Group
   */
  def drawStatsText(msg: String, x: Int, y: Int): Text[Material.ImageEffects] =
    Text(msg, x, y, 1, Assets.Fonts.fontKey, Assets.Fonts.fontMaterial)

  /**
   * Draw stats value with icon in correcto position inside HUD
   * @param character
   *   [[CharacterModel]]
   * @return
   *   Group
   */
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
      .addChild(Assets.HUD.Graphics.FireSpeed.moveTo(0, 160))
      .addChild(drawStatsText(FireSpeed @@ character.stats, 30, 166))

  /**
   * Draw all HUD: Life and Stats
   * @param context
   *   [[FrameContext]]
   * @param character
   *   [[CharacterModel]]
   * @return
   *   Group
   */
  def draw(context: FrameContext[StartupData], character: CharacterModel): Group =
    Group()
      .addChild(drawLife(character))
      .addChild(drawStats(character))
      .moveTo(10, 10)
}
