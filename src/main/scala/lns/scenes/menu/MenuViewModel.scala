package lns.scenes.menu
import indigo._
import indigoextras.ui.*

case class MenuViewModel(button: Button)

object MenuViewModel {
  val buttonAssets: ButtonAssets =
    ButtonAssets(
      up = Graphic(0, 0, 221, 70, 2, Material.Bitmap(AssetName("startButton"))).withCrop(0, 0, 221, 70),
      over = Graphic(0, 0, 221, 70, 2, Material.Bitmap(AssetName("startButton"))).withCrop(0, 0, 221, 70),
      down = Graphic(0, 0, 221, 70, 2, Material.Bitmap(AssetName("startButton"))).withCrop(0, 0, 221, 70)
    )

  val initial: MenuViewModel = MenuViewModel(
    button = Button(
      buttonAssets = buttonAssets,
      bounds = Rectangle(10, 10,221, 70),
      depth = Depth(2)
    ).withUpActions(StartEvent)
  )

}

case object StartEvent extends GlobalEvent
