package lns.scenes.game.shot

import indigo.*
import indigo.shared.scenegraph.Shape
import lns.StartupData
import lns.scenes.game.anything.AnythingView

/**
 * Character Isaac view based on CharacterModel and built grouping its elements head, body and shadow
 */
case class ShotView() extends AnythingView {

  type Model     = ShotModel
  type ViewModel = Unit
  type View      = Group

  val scale: Int = 2

  def shot(model: Model): Shape =
    Shape
      .Circle(
        center = Point(model.boundingBox.width.toInt / 2, model.boundingBox.height.toInt),
        radius = model.boundingBox.width.toInt,
        fill = Fill.Color(RGBA(1, 0, 0, 1)),
        stroke = Stroke(2, RGBA.Black)
      )

  def view(contex: FrameContext[StartupData], model: Model, viewModel: ViewModel): View =
    Group()
      .addChild(shot(model))
      .withRef(model.boundingBox.width.toInt / 2, model.boundingBox.height.toInt / 2)
      .withScale(Vector2(scale, scale))
}
