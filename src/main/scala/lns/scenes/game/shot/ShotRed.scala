package lns.scenes.game.shot

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Animations.*
import lns.core.anythingAssets.ShotAsset
import lns.scenes.game.anything.*

/**
 * Yellow shot view elements builder
 */
trait ShotRed extends ShotAsset {

  def drawShot: Shape =
    Shape
      .Circle(
        center = Point(width / 2, height / 2),
        radius = width / 2,
        fill = Fill.Color(RGBA(1, 0, 0, 1)),
        stroke = Stroke(2, RGBA.Black)
      )
}
