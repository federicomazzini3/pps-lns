package lns.scenes.game.enemy.mask

import indigo.*
import indigo.shared.scenegraph.{ Graphic, Shape }
import lns.core.Assets
import lns.scenes.game.anything.{ DynamicState, FireState }

/**
 * Isaac Character view elements builder
 */
trait Mask {

  import Assets.Enemies.Mask.*

  val shadowModel: Shape =
    Shape
      .Circle(
        center = Point(width / 2, height + width / 4),
        radius = width / 3,
        Fill.Color(RGBA(0, 0, 0, 0.4))
      )
      .scaleBy(1, 0.25)

  val boundingModel: Shape =
    Shape.Box(
      Rectangle(Point(0, 0), Size(width, height - offsetY)),
      Fill.Color(RGBA(1, 1, 1, 0.5))
    )

  def headCrop(state: FireState | DynamicState): Rectangle = state match {
    case FireState.FIRE_LEFT | DynamicState.MOVE_LEFT =>
      Rectangle(2, 0, width, height)
    case FireState.FIRE_RIGHT | DynamicState.MOVE_RIGHT =>
      Rectangle(2, 32, width, height)
    case FireState.FIRE_UP | DynamicState.MOVE_UP =>
      Rectangle(34, 0, width, height)
    case _ =>
      Rectangle(34, 32, width, height)
  }

  def headDirection(model: MaskModel): Rectangle = model.isFiring() match {
    case true => headCrop(model.getFireState())
    case _    => headCrop(model.getDynamicState())
  }

  def headView(model: MaskModel): Graphic[Material.Bitmap] =
    Graphic(headDirection(model), 1, Material.Bitmap(name))
      .withRef(0, 0)
      .moveTo(0, 0)

}
