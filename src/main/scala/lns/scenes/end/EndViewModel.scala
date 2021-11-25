package lns.scenes.end

import indigo.*
import indigo.shared.datatypes.Rectangle
import indigoextras.ui.Button
import lns.StartupData
import lns.core.Assets

case class EndViewModel(button: Button)

object EndViewModel {

  def initial(startupData: StartupData): EndViewModel = EndViewModel(
    button = Button(
      buttonAssets = Assets.Buttons.Graphics.start,
      bounds = Rectangle(
        startupData.screenDimensions.horizontalCenter - 110,
        startupData.screenDimensions.verticalCenter - 35,
        221,
        70
      ),
      depth = Depth(2)
    ).withUpActions(Restart)
  )

}
