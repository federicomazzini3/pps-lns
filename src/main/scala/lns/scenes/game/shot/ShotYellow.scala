package lns.scenes.game.shot

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Animations.*
import lns.core.anythingAssets.AnythingAsset
import lns.scenes.game.anything.*

/**
 * Yellow shot view elements builder
 */
trait ShotYellow extends AnythingAsset {
  override val name: String  = ""
  override val width: Int    = 40
  override val height: Int   = 40
  override val offsetY: Int  = 0
  override val scale: Double = 1

  def shot: Shape =
    Shape
      .Circle(
        center = Point(width / 2, height / 2),
        radius = width / 2,
        fill = Fill.Color(RGBA(1, 1, 0, 1)),
        stroke = Stroke(2, RGBA.Black)
      )
}
