package lns.scenes.menu
import indigo.*
import indigoextras.ui.*
import lns.StartupData
import lns.core.Assets

/**
 * Scene Menu ViewModel which stores presentation state
 * @param button data and state
 */
case class MenuViewModel(button: Button)

object MenuViewModel {

  def initial(startupData:StartupData): MenuViewModel = MenuViewModel(
    button = Button(
      buttonAssets = Assets.Buttons.Graphics.start,
      bounds = Rectangle(startupData.screenDimensions.horizontalCenter-110, startupData.screenDimensions.verticalCenter-35,221, 70),
      depth = Depth(2)
    ).withUpActions(StartEvent)
  )

}
