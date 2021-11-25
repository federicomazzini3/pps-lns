package lns.scenes.end

import indigoextras.ui.Button
import indigo.shared.datatypes.{ Rectangle, Vector2 }
import indigo.shared.scenegraph.{ Group, SceneUpdateFragment, Text }
import lns.core.Assets

object EndView {

  def draw(screenDimensions: Rectangle, button: Button): SceneUpdateFragment = {

    def text(message: String, horizontalOffset: Int, verticalOffset: Int) =
      Text(
        message,
        screenDimensions.horizontalCenter + horizontalOffset,
        screenDimensions.verticalCenter + verticalOffset,
        1,
        Assets.Fonts.fontKey,
        Assets.Fonts.fontMaterial
      ).alignCenter
        .withScale(Vector2(3, 3))

    val exam: String =
      "Paradigmi\ndi Programmazione\ne Sviluppo"
    val credits: String =
      "Credits\nAlan Mancini\nFederico Mazzini\nMatteo Brocca"

    SceneUpdateFragment(
      text(exam, 0, -200),
      text(credits, 0, 100),
      button.draw
    )
  }
}
